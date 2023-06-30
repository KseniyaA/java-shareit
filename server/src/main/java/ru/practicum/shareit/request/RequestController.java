package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDtoRequest;
import ru.practicum.shareit.request.dto.RequestDtoResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/requests")
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    public RequestDtoResponse add(@RequestHeader("X-Sharer-User-Id") long requesterId,
                                  @RequestBody RequestDtoRequest requestDtoRequest) {
        Request request = requestService.add(RequestMapper.toRequest(requestDtoRequest), requesterId);
        return RequestMapper.toRequestDtoResponse(request);
    }

    /**
     * GET /requests — получить список своих запросов вместе с данными об ответах на них
     */
    @GetMapping
    public List<RequestDtoResponse> getRequestsByUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        List<Request> requests = requestService.getAllByUser(userId);
        return RequestMapper.toRequestDtoResponseList(requests);
    }

    /**
     * GET /requests/all?from={from}&size={size} — получить список запросов, созданных другими пользователями
     */
    @GetMapping("/all")
    public List<RequestDtoResponse> getRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @RequestParam(required = false) Integer from,
                                          @RequestParam(required = false) Integer size) {
        List<Request> requests = requestService.getAll(userId, from, size);
        return RequestMapper.toRequestDtoResponseList(requests);
    }

    @GetMapping("/{requestId}")
    public RequestDtoResponse getRequestsById(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @PathVariable("requestId") long id) {
        return RequestMapper.toRequestDtoResponse(requestService.getById(id, userId));
    }
}
