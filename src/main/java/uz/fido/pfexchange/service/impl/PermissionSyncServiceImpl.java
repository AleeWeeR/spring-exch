package uz.fido.pfexchange.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.fido.pfexchange.config.Authority;
import uz.fido.pfexchange.entity.Permission;
import uz.fido.pfexchange.repository.PermissionRepository;
import uz.fido.pfexchange.service.PermissionSyncService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionSyncServiceImpl implements PermissionSyncService {

    private static final String ACTIVE = "Y";
    private static final String INACTIVE = "N";
    private static final Long SYSTEM_USER_ID = 1L;

    private final PermissionRepository permissionRepository;

    @Override
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncPermissions() {
        log.info("Starting permission synchronization...");

        Set<String> enumCodes = Arrays.stream(Authority.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

        Map<String, Permission> existingPermissions = permissionRepository.findAll().stream()
            .collect(Collectors.toMap(Permission::getCode, Function.identity()));

        SyncResult result = new SyncResult();

        processEnumAuthorities(existingPermissions, result);
        processOrphanedPermissions(enumCodes, existingPermissions, result);

        logSyncSummary(result);
    }

    private void processEnumAuthorities(Map<String, Permission> existingPermissions, SyncResult result) {
        for (Authority authority : Authority.values()) {
            String code = authority.name();
            Permission existing = existingPermissions.get(code);

            if (existing == null) {
                createPermission(authority);
                result.added.add(code);
            } else {
                processExistingPermission(authority, existing, result);
            }
        }
    }

    private void processExistingPermission(Authority authority, Permission existing, SyncResult result) {
        String code = authority.name();
        boolean nameChanged = !existing.getName().equals(authority.getDisplayName());
        boolean isInactive = INACTIVE.equals(existing.getIsActiveFlag());

        if (nameChanged) {
            updatePermissionName(code, authority.getDisplayName());
            result.updated.add(code);
        }

        if (isInactive) {
            reactivatePermission(code);
            result.reactivated.add(code);
        }
    }

    private void processOrphanedPermissions(Set<String> enumCodes, 
                                            Map<String, Permission> existingPermissions, 
                                            SyncResult result) {
        List<String> orphanedCodes = existingPermissions.entrySet().stream()
            .filter(entry -> !enumCodes.contains(entry.getKey()))
            .filter(entry -> ACTIVE.equals(entry.getValue().getIsActiveFlag()))
            .map(Map.Entry::getKey)
            .toList();

        if (orphanedCodes.isEmpty()) {
            return;
        }

        deactivatePermissions(orphanedCodes);
        result.deactivated.addAll(orphanedCodes);

        orphanedCodes.forEach(code ->
            log.warn("Permission '{}' exists in database but not in Authority enum. Marked as inactive.", code)
        );
    }

    private void createPermission(Authority authority) {
        Permission permission = Permission.builder()
            .code(authority.name())
            .name(authority.getDisplayName())
            .isActiveFlag(ACTIVE)
            .createdBy(SYSTEM_USER_ID)
            .lastUpdatedBy(SYSTEM_USER_ID)
            .build();

        permissionRepository.save(permission);
        log.info("Created new permission: {} ({})", authority.name(), authority.getDisplayName());
    }

    private void updatePermissionName(String code, String newName) {
        permissionRepository.updateNameByCode(code, newName);
        log.info("Updated permission name: {} -> '{}'", code, newName);
    }

    private void reactivatePermission(String code) {
        permissionRepository.updateActiveFlagByCodes(List.of(code), ACTIVE);
        log.info("Reactivated permission: {}", code);
    }

    private void deactivatePermissions(List<String> codes) {
        permissionRepository.updateActiveFlagByCodes(codes, INACTIVE);
        log.info("Deactivated {} orphaned permissions", codes.size());
    }

    private void logSyncSummary(SyncResult result) {
        log.info("Permission synchronization completed:");
        log.info("  - Added: {}", result.added.size());
        log.info("  - Updated: {}", result.updated.size());
        log.info("  - Reactivated: {}", result.reactivated.size());
        log.info("  - Deactivated: {}", result.deactivated.size());

        if (result.isEmpty()) {
            log.info("  - No changes required");
        }
    }

    private static class SyncResult {
        final List<String> added = new ArrayList<>();
        final List<String> updated = new ArrayList<>();
        final List<String> reactivated = new ArrayList<>();
        final List<String> deactivated = new ArrayList<>();

        boolean isEmpty() {
            return added.isEmpty() && updated.isEmpty() &&
                   reactivated.isEmpty() && deactivated.isEmpty();
        }
    }
}