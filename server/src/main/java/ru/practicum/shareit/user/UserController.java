package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private static final String USER_ID = "userId";
    private final UserService userService;

    @PostMapping
    public UserDtoResponse create(@RequestBody UserDtoRequest userDtoRequest) {
        log.info("Получен запрос POST /users с параметрами {}", userDtoRequest);
        User user = userService.create(UserMapper.toUser(userDtoRequest));
        return UserMapper.toUserDtoResponse(user);
    }

    @PatchMapping("/{userId}")
    public UserDtoResponse update(@RequestBody UserDtoRequest userDtoRequest,
                                  @PathVariable(USER_ID) long userId) {
        log.info("Получен запрос PATCH /users/{userId} с параметрами dto = {} и userId = {}", userDtoRequest, userId);
        User user = userService.update(UserMapper.toUser(userDtoRequest, userId));
        return UserMapper.toUserDtoResponse(user);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable(USER_ID) long userId) {
        log.info("Получен запрос DELETE /users/{userId} с userId = {}", userId);
        userService.deleteById(userId);
    }

    @GetMapping("/{id}")
    public UserDtoResponse get(@PathVariable long id) {
        log.info("Получен запрос GET /users/{id} с параметрами id = {}", id);
        return UserMapper.toUserDtoResponse(userService.getById(id));
    }

    @GetMapping
    public List<UserDtoResponse> getAll() {
        log.info("Получен запрос GET /users");
        return UserMapper.toUserDtoResponseList(userService.getAll());
    }
}
