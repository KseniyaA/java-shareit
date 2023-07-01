package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.Marker;
import ru.practicum.shareit.common.ValidateFromIfPresent;
import ru.practicum.shareit.common.ValidateSizeIfPresent;
import ru.practicum.shareit.request.dto.RequestDtoRequest;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/requests")
public class RequestController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(X_SHARER_USER_ID) long requesterId,
                                      @RequestBody @Validated(Marker.OnCreate.class) RequestDtoRequest requestDtoRequest) {
        log.info("Получен запрос POST /requests с параметрами userId = {}, dto = {}", requesterId, requestDtoRequest);
        return requestClient.add(requestDtoRequest, requesterId);
    }

    /**
     * GET /requests — получить список своих запросов вместе с данными об ответах на них
     */
    @GetMapping
    public ResponseEntity<Object> getRequestsByUser(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Получен запрос GET /requests с параметрами userId = {}", userId);
        return requestClient.getAllByUser(userId);
    }

    /**
     * GET /requests/all?from={from}&size={size} — получить список запросов, созданных другими пользователями
     */
    @GetMapping("/all")
    public ResponseEntity<Object> getRequests(@RequestHeader(X_SHARER_USER_ID) long userId,
                                              @RequestParam(required = false) @ValidateFromIfPresent Integer from,
                                              @RequestParam(required = false) @ValidateSizeIfPresent Integer size) {
        log.info("Получен запрос GET /requests/all с параметрами userId = {}, from = {}, size = {}", userId, from, size);
        return requestClient.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestsById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                  @PathVariable("requestId") long id) {
        log.info("Получен запрос GET /requests/requestId с параметрами userId = {}, requestId = {}", userId, id);
        return requestClient.getById(id, userId);
    }
}
