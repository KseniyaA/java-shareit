package ru.practicum.shareit.user;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class UserMapper {

    public User toUser(UserDtoRequest userDtoRequest) {
        return User.builder()
                .id(userDtoRequest.getId())
                .name(userDtoRequest.getName())
                .email(userDtoRequest.getEmail())
                .build();
    }


    public UserDtoRequest toUserDtoRequest(User user) {
        return UserDtoRequest.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public UserDtoResponse toUserDtoResponse(User user) {
        return UserDtoResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public List<UserDtoResponse> toUserDtoResponseList(List<User> users) {
        return users.stream().map(UserMapper::toUserDtoResponse).collect(Collectors.toList());
    }

    public User toUser(UserDtoRequest userDtoRequest, long userId) {
        User.UserBuilder builder = User.builder().id(userId);
        if (Optional.ofNullable(userDtoRequest.getEmail()).isPresent()) {
            builder.email(userDtoRequest.getEmail());
        }
        if (Optional.ofNullable(userDtoRequest.getName()).isPresent()) {
            builder.name(userDtoRequest.getName());
        }
        return builder.build();
    }
}
