package uz.fido.pfexchange.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import uz.fido.pfexchange.dto.RegisterDto;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.log.DownloadedSystemLogFile;
import uz.fido.pfexchange.dto.log.LogDto;
import uz.fido.pfexchange.dto.log.LogFilterRequest;
import uz.fido.pfexchange.dto.log.LogStatsDto;
import uz.fido.pfexchange.dto.log.SystemLogDto;
import uz.fido.pfexchange.dto.log.SystemLogFileInfoDto;
import uz.fido.pfexchange.dto.user.PermissionDto;
import uz.fido.pfexchange.dto.user.UserDto;
import uz.fido.pfexchange.dto.user.UserPermissionsRequest;
import uz.fido.pfexchange.service.LogQueryService;
import uz.fido.pfexchange.service.PermissionService;
import uz.fido.pfexchange.service.SystemLogService;
import uz.fido.pfexchange.service.UserService;
import uz.fido.pfexchange.utils.ResponseBuilder;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserService userService;
    private final LogQueryService logQueryService;
    private final SystemLogService systemLogService;
    private final PermissionService permissionService;

    @GetMapping("/users/{id}")
    public ResponseEntity<ResponseWrapperDto<UserDto>> getById(@PathVariable Long id) {
        UserDto user = userService.getById(id);
        return ResponseBuilder.ok(user);
    }

    @GetMapping("/users")
    public ResponseEntity<ResponseWrapperDto<List<UserDto>>> getAll() {
        List<UserDto> users = userService.getAll();
        return ResponseBuilder.ok(users);
    }

    @PostMapping("/users/register")
    public ResponseEntity<ResponseWrapperDto<Object>> register(
            @Valid @RequestBody RegisterDto register) {
        userService.register(register);
        return ResponseBuilder.getSuccess(HttpStatus.OK, "Ma'lumotlar muvaffaqqiyatli saqlandi!");
    }

    @PutMapping("/users")
    public ResponseEntity<ResponseWrapperDto<UserDto>> save(@RequestBody UserDto user) {
        UserDto savedUser = userService.save(user);
        return ResponseBuilder.ok(savedUser);
    }

    @GetMapping("/users/exists")
    public ResponseEntity<ResponseWrapperDto<Boolean>> existsByUsername(
            @RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseBuilder.ok(exists);
    }

    @GetMapping("/permissions")
    public ResponseEntity<ResponseWrapperDto<List<PermissionDto>>> getAllPermissions() {
        List<PermissionDto> permissions = permissionService.getAllPermissions();
        return ResponseBuilder.ok(permissions);
    }

    @GetMapping("/users/permissions/{username}")
    public ResponseEntity<ResponseWrapperDto<List<PermissionDto>>> getPermissions(
            @PathVariable String username) {
        List<PermissionDto> permissions = permissionService.getPermissions(username);
        return ResponseBuilder.ok(permissions);
    }

    @PostMapping("/users/permissions")
    public ResponseEntity<ResponseWrapperDto<List<PermissionDto>>> savePermissions(
            @RequestBody UserPermissionsRequest request) {
        List<PermissionDto> savedPermissions = permissionService.savePermissions(request);
        return ResponseBuilder.ok(savedPermissions);
    }

    @GetMapping("/logs")
    public ResponseEntity<ResponseWrapperDto<Page<LogDto>>> getLogs(
            @ModelAttribute LogFilterRequest filter) {
        Page<LogDto> logs = logQueryService.findLogs(filter);
        return ResponseBuilder.ok(logs);
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<ResponseWrapperDto<LogDto>> getLogById(@PathVariable Long id) {
        return logQueryService
                .findById(id)
                .map(ResponseBuilder::ok)
                .orElse(ResponseBuilder.notFound("Log topilmadi"));
    }

    @GetMapping("/logs/correlation/{correlationId}")
    public ResponseEntity<ResponseWrapperDto<List<LogDto>>> getByCorrelationId(
            @PathVariable String correlationId) {
        List<LogDto> logs = logQueryService.findByCorrelationId(correlationId);
        return ResponseBuilder.ok(logs);
    }

    @GetMapping("/logs/stats")
    public ResponseEntity<ResponseWrapperDto<LogStatsDto>> getStats(
            @RequestParam(defaultValue = "24") int hours) {
        LogStatsDto stats = logQueryService.getStats(hours);
        return ResponseBuilder.ok(stats);
    }

    @GetMapping("/logs/system/latest")
    public ResponseEntity<ResponseWrapperDto<SystemLogDto>> getLatestSystemLogs(
            @RequestParam String file, @RequestParam(defaultValue = "100") int lines) {
        SystemLogDto latestLogs = systemLogService.getLatestSystemLogs(file, lines);
        return ResponseBuilder.ok(latestLogs);
    }

    @GetMapping("/logs/system/files")
    public ResponseEntity<ResponseWrapperDto<List<SystemLogFileInfoDto>>> listSystemLogFiles() {
        List<SystemLogFileInfoDto> files = systemLogService.listLogFiles();
        return ResponseBuilder.ok(files);
    }

    @GetMapping("/logs/system/download")
    public ResponseEntity<Resource> downloadSystemLog(@RequestParam String file)
            throws IOException {

        DownloadedSystemLogFile download = systemLogService.prepareDownload(file);

        return ResponseEntity.ok()
                .contentType(download.mediaType())
                .contentLength(download.contentLength())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.fileName() + "\"")
                .body(download.resource());
    }
}
