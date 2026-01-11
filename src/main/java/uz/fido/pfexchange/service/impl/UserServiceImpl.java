package uz.fido.pfexchange.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.RegisterDto;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.user.UserDto;
import uz.fido.pfexchange.entity.User;
import uz.fido.pfexchange.exception.RestException;
import uz.fido.pfexchange.mapper.UserMapper;
import uz.fido.pfexchange.repository.UserRepository;
import uz.fido.pfexchange.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto getById(Long id) {
        return userRepository
            .findById(id)
            .map(UserMapper::toDto)
            .orElseThrow(() ->
                new UsernameNotFoundException("Foydalanuvchi topilmadi")
            );
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository
            .findAll()
            .stream()
            .sorted(Comparator.comparing(User::getId))
            .map(UserMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {
        User user = userRepository
            .findByUsernameAndIsActiveFlag(username, "Y")
            .orElseThrow(() ->
                new UsernameNotFoundException("Foydalanuvchi topilmadi")
            );

        user.getUserPermissions().size();

        return user;
    }

    @Override
    public UserDto save(UserDto userDto) {
        User user = userRepository.findById(userDto.getId()).orElse(new User());

        user.setUsername(userDto.getUsername());
        user.setName(userDto.getName());
        user.setAddInfo(userDto.getAddInfo());
        user.setIsActiveFlag(userDto.getIsActiveFlag());

        User savedRecord = userRepository.save(user);
        return UserMapper.toDto(savedRecord);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository
            .findByUsernameAndIsActiveFlag(username, "Y")
            .isPresent();
    }

    @Override
    @Transactional
    public void register(RegisterDto register) {
        if (
            !Objects.equals(
                register.getPassword(),
                register.getConfirmPassword()
            )
        ) {
            throw RestException.restThrow(
                ResponseWrapperDto.builder()
                    .code(Constants.ERROR)
                    .message("Parollar mos emas!")
                    .build(),
                HttpStatus.PRECONDITION_FAILED
            );
        }

        if (existsByUsername(register.getUsername().trim())) {
            throw RestException.restThrow(
                ResponseWrapperDto.builder()
                    .code(Constants.ERROR)
                    .message("Foydalanuvchi nomi allaqachon mavjud!")
                    .build(),
                HttpStatus.CONFLICT
            );
        }

        var user = User.builder()
            .username(register.getUsername().trim())
            .password(passwordEncoder.encode(register.getPassword()))
            .name(register.getUsername().trim())
            .addInfo(register.getAddInfo())
            .isActiveFlag("Y")
            .build();

        userRepository.save(user);
    }
}
