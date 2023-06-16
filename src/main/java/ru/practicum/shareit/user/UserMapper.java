package ru.practicum.shareit.user;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class UserMapper {

    public User convertUserDtoToUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }


    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public List<UserDto> toUserDtoList(List<User> users) {
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    public User toUser(UserDto userDto, long userId) {
        User.UserBuilder builder = User.builder().id(userId);
        if (Optional.ofNullable(userDto.getEmail()).isPresent()) {
            builder.email(userDto.getEmail());
        }
        if (Optional.ofNullable(userDto.getName()).isPresent()) {
            builder.name(userDto.getName());
        }
        return builder.build();
    }
}
