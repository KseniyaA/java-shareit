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
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader("X-Sharer-User-Id") long requesterId,
                                      @Validated(Marker.OnCreate.class) @RequestBody RequestDtoRequest requestDtoRequest) {
        return requestClient.add(requestDtoRequest, requesterId);
    }

    /**
     * GET /requests — получить список своих запросов вместе с данными об ответах на них
     */
    @GetMapping
    public ResponseEntity<Object> getRequestsByUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        return requestClient.getAllByUser(userId);
    }

    /**
     * GET /requests/all?from={from}&size={size} — получить список запросов, созданных другими пользователями
     */
    @GetMapping("/all")
    public ResponseEntity<Object> getRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(required = false) @ValidateFromIfPresent Integer from,
                                              @RequestParam(required = false) @ValidateSizeIfPresent Integer size) {
        return requestClient.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestsById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @PathVariable("requestId") long id) {
        return requestClient.getById(id, userId);
    }
}
