package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@RequestBody UserDto userDto) {
        log.info("Получен запрос POST /users с параметрами {}", userDto);
        return userService.create(UserMapper.convertUserDtoToUser(userDto));
    }

    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody UserDto userDtoUpdateRequest,
                          @PathVariable("userId") long userId) {
        log.info("Получен запрос PATCH /users с параметрами {} и id = {}", userDtoUpdateRequest, userId);
        User user = userService.update(UserMapper.toUser(userDtoUpdateRequest, userId));
        return UserMapper.toUserDto(user);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable("userId") long userId) {
        log.info("Получен запрос DELETE /users с id = {}", userId);
        userService.deleteById(userId);
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable long id) {
        log.info("Получен запрос GET /users/{id} с параметрами id = {}", id);
        return UserMapper.toUserDto(userService.getById(id));
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info("Получен запрос GET /users");
        return UserMapper.toUserDtoList(userService.getAll());
    }
}
