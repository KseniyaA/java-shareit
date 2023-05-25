package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoCreateRequest;
import ru.practicum.shareit.user.dto.UserDtoUpdateRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@RestController
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public User create(@RequestBody UserDtoCreateRequest userDto) {
        log.info("Получен запрос POST /users с параметрами {}", userDto);
        return userService.create(UserMapper.convertUserDtoCreateRequestToUser(userDto));
    }

    @PatchMapping("/users/{userId}")
    public UserDto update(@RequestBody UserDtoUpdateRequest userDtoUpdateRequest,
                          @PathVariable("userId") long userId) {
        log.info("Получен запрос PATCH /users с параметрами {} и id = {}", userDtoUpdateRequest, userId);
        User user = userService.update(UserMapper.toUser(userDtoUpdateRequest, userId));
        return UserMapper.toUserDto(user);
    }

    @DeleteMapping("/users/{userId}")
    public void delete(@PathVariable("userId") long userId) {
        log.info("Получен запрос DELETE /users с id = {}", userId);
        userService.deleteById(userId);
    }

    @GetMapping("users/{id}")
    public UserDto get(@PathVariable long id) {
        log.info("Получен запрос GET /users/{id} с параметрами id = {}", id);
        return UserMapper.toUserDto(userService.getById(id));
    }

    @GetMapping("users")
    public List<UserDto> getAll() {
        log.info("Получен запрос GET /users");
        return UserMapper.toUserDtoList(userService.getAll());
    }
}
