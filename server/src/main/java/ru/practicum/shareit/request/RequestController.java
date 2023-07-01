package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDtoRequest;
import ru.practicum.shareit.request.dto.RequestDtoResponse;

import java.util.List;

import static ru.practicum.shareit.common.Constants.X_SHARER_USER_ID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/requests")
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    public RequestDtoResponse add(@RequestHeader(X_SHARER_USER_ID) long requesterId,
                                  @RequestBody RequestDtoRequest requestDtoRequest) {
        log.info("Получен запрос POST /requests с параметрами userId = {}, dto = {}", requesterId, requestDtoRequest);
        Request request = requestService.add(RequestMapper.toRequest(requestDtoRequest), requesterId);
        return RequestMapper.toRequestDtoResponse(request);
    }

    /**
     * GET /requests — получить список своих запросов вместе с данными об ответах на них
     */
    @GetMapping
    public List<RequestDtoResponse> getRequestsByUser(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Получен запрос GET /requests с параметрами userId = {}", userId);
        List<Request> requests = requestService.getAllByUser(userId);
        return RequestMapper.toRequestDtoResponseList(requests);
    }

    /**
     * GET /requests/all?from={from}&size={size} — получить список запросов, созданных другими пользователями
     */
    @GetMapping("/all")
    public List<RequestDtoResponse> getRequests(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                @RequestParam(required = false) Integer from,
                                                @RequestParam(required = false) Integer size) {
        log.info("Получен запрос GET /requests/all?from={from}&size={size} с параметрами userId = {}, from = {}, size = {}", userId, from, size);
        List<Request> requests = requestService.getAll(userId, from, size);
        return RequestMapper.toRequestDtoResponseList(requests);
    }

    @GetMapping("/{requestId}")
    public RequestDtoResponse getRequestsById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                              @PathVariable("requestId") long id) {
        log.info("Получен запрос GET /requests/{requestId} с параметрами userId = {}, requestId = {}", userId, id);
        return RequestMapper.toRequestDtoResponse(requestService.getById(id, userId));
    }
}
