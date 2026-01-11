package uz.fido.pfexchange.service;

import java.util.List;
import uz.fido.pfexchange.dto.user.PermissionDto;
import uz.fido.pfexchange.dto.user.UserPermissionsRequest;

public interface PermissionService {
    List<PermissionDto> getPermissions(String username);
    
    List<PermissionDto> getAllPermissions();
    
    List<PermissionDto> savePermissions(UserPermissionsRequest request);
}
