package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDtoForRequestResponse;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestDtoRequest;
import ru.practicum.shareit.request.dto.RequestDtoResponse;
import ru.practicum.shareit.user.UserMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {
    public RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
                .description(request.getDescription())
                .requester(UserMapper.toUserDtoRequest(request.getRequester()))
                .build();
    }

    public Request toRequest(RequestDtoRequest requestDto) {
        return Request.builder()
                .description(requestDto.getDescription())
                .build();
    }

    public static RequestDtoResponse toRequestDtoResponse(Request request) {
        List<ItemDtoForRequestResponse> items = request.getItems() == null || request.getItems().isEmpty() ?
                Collections.emptyList() :
                request.getItems().stream()
                        .map(ItemMapper::toItemDtoForRequestResponse)
                        .collect(Collectors.toList());
        return RequestDtoResponse.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items)
                .build();
    }

    public static List<RequestDtoResponse> toRequestDtoResponseList(List<Request> requests) {
        List<RequestDtoResponse> list = new ArrayList<>();
        for (Request request : requests) {
            list.add(toRequestDtoResponse(request));
        }
        return list;
    }
}
