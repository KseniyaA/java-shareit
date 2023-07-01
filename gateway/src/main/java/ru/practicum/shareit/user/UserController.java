package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.Marker;
import ru.practicum.shareit.user.dto.UserDtoRequest;

@RestController
@Slf4j
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private static final String USER_ID = "userId";
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Validated({Marker.OnCreate.class}) UserDtoRequest userDtoRequest) {
        log.info("Получен запрос POST /users с параметрами {}", userDtoRequest);
        return userClient.create(userDtoRequest);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@RequestBody @Validated({Marker.OnUpdate.class}) UserDtoRequest userDtoRequest,
                                         @PathVariable(USER_ID) long userId) {
        log.info("Получен запрос PATCH /users/{userId} с параметрами dto = {} и userId = {}", userDtoRequest, userId);
        return userClient.update(userDtoRequest, userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable(USER_ID) long userId) {
        log.info("Получен запрос DELETE /users/{userId} с userId = {}", userId);
        userClient.deleteById(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable long id) {
        log.info("Получен запрос GET /users/{id} с параметрами id = {}", id);
        return userClient.getById(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Получен запрос GET /users");
        return userClient.getAll();
    }
}
