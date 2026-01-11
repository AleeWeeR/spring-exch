package uz.fido.pfexchange.mapper;

import java.util.List;

import uz.fido.pfexchange.dto.user.PermissionDto;
import uz.fido.pfexchange.entity.Permission;

public class PermissionMapper {

    public static PermissionDto toDto(Permission permission, List<String> endPoints) {
        return PermissionDto.builder()
            .code(permission.getCode())
            .name(permission.getName())
            .addInfo(permission.getAddInfo())
            .endPoints(endPoints)
            .build();
    }
    
    public static Permission toEntity(PermissionDto permissionDto) {
        return Permission.builder()
            .code(permissionDto.getCode())
            .name(permissionDto.getName())
            .addInfo(permissionDto.getAddInfo())
            .isActiveFlag("Y")
            .build();
    }
}
