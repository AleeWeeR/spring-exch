package uz.fido.pfexchange.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import uz.fido.pfexchange.dto.user.PermissionDto;
import uz.fido.pfexchange.dto.user.UserPermissionsRequest;
import uz.fido.pfexchange.entity.Permission;
import uz.fido.pfexchange.entity.User;
import uz.fido.pfexchange.entity.UserPermission;
import uz.fido.pfexchange.mapper.PermissionMapper;
import uz.fido.pfexchange.repository.PermissionRepository;
import uz.fido.pfexchange.repository.UserRepository;
import uz.fido.pfexchange.service.PermissionService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RequestMappingHandlerMapping handlerMapping;

    public PermissionServiceImpl(
            UserRepository userRepository,
            PermissionRepository permissionRepository,
            @Qualifier("requestMappingHandlerMapping")
                    RequestMappingHandlerMapping handlerMapping) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.handlerMapping = handlerMapping;
    }

    @Override
    public List<PermissionDto> getPermissions(String username) {
        User user =
                userRepository
                        .findByUsernameAndIsActiveFlag(username, "Y")
                        .orElseThrow(
                                () -> new UsernameNotFoundException("Foydalanuvchi topilmadi"));

        return user.getUserPermissions().stream()
                .filter(p -> p.getIsActiveFlag().equals("Y"))
                .map(
                        p ->
                                PermissionMapper.toDto(
                                        p.getPermission(),
                                        getEndPoints(p.getPermission().getCode())))
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .filter(p -> p.getIsActiveFlag().equals("Y"))
                .map(p -> PermissionMapper.toDto(p, getEndPoints(p.getCode())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PermissionDto> savePermissions(UserPermissionsRequest request) {
        User user =
                userRepository
                        .findByUsernameAndIsActiveFlag(request.getUsername(), "Y")
                        .orElseThrow(
                                () -> new UsernameNotFoundException("Foydalanuvchi topilmadi"));

        if (user.getUserPermissions() != null) {
            user.getUserPermissions().clear();
        } else {
            user.setUserPermissions(new HashSet<>());
        }

        List<Permission> newPermissions =
                request.getPermissions().stream()
                        .map(PermissionMapper::toEntity)
                        .collect(Collectors.toList());

        for (Permission permission : newPermissions) {
            UserPermission userPermission =
                    UserPermission.builder()
                            .user(user)
                            .permission(permission)
                            .isActiveFlag("Y")
                            .build();

            user.getUserPermissions().add(userPermission);
        }

        userRepository.save(user);

        return newPermissions.stream()
                .map(p -> PermissionMapper.toDto(p, getEndPoints(p.getCode())))
                .collect(Collectors.toList());
    }

    public List<String> getEndPoints(String permissionCode) {
        List<String> matchingEndpoints = new ArrayList<>();

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            PreAuthorize preAuth = handlerMethod.getMethodAnnotation(PreAuthorize.class);

            if (preAuth != null && preAuth.value().contains(permissionCode)) {
                RequestMappingInfo mappingInfo = entry.getKey();
                matchingEndpoints.addAll(
                        mappingInfo.getPathPatternsCondition() != null
                                ? mappingInfo.getPathPatternsCondition().getPatterns().stream()
                                        .map(Object::toString)
                                        .collect(Collectors.toSet())
                                : mappingInfo.getPatternsCondition().getPatterns());
            }
        }

        return matchingEndpoints;
    }
}
