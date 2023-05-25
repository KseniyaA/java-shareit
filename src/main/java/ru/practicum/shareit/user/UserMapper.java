package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;
import ru.practicum.shareit.user.dto.UserDtoUpdateRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserMapper {

    public static User convertUserDtoCreateRequestToUser(UserDtoCreateRequest userDto) {
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static List<UserDto> toUserDtoList(List<User> users) {
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    public static User toUser(UserDtoUpdateRequest userDto, long userId) {
        User.UserBuilder userBuilder = User.builder()
                .id(userId);
        if (Optional.ofNullable(userDto.getEmail()).isPresent()) {
            userBuilder.email(userDto.getEmail());
        }
        if (Optional.ofNullable(userDto.getName()).isPresent()) {
            userBuilder.name(userDto.getName());
        }
        return userBuilder.build();
    }
}
