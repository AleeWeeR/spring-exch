package uz.fido.pfexchange.mapper;

import uz.fido.pfexchange.dto.user.UserDto;
import uz.fido.pfexchange.entity.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .name(user.getName())
            .addInfo(user.getAddInfo())
            .isActiveFlag(user.getIsActiveFlag())
            .build();
    }

    public static User toEntity(UserDto userDto) {
        return User.builder()
            .id(userDto.getId())
            .username(userDto.getUsername())
            .name(userDto.getName())
            .addInfo(userDto.getAddInfo())
            .build();
    }
}
