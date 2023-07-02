package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.request.dto.RequestDtoRequest;
import ru.practicum.shareit.request.dto.RequestDtoResponse;
import ru.practicum.shareit.user.UserMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {

    public Request toRequest(RequestDtoRequest requestDto) {
        return Request.builder()
                .description(requestDto.getDescription())
                .build();
    }

    public static RequestDtoResponse toRequestDtoResponse(Request request) {
        List<ItemDtoResponse> items = request.getItems() == null || request.getItems().isEmpty() ?
                Collections.emptyList() :
                request.getItems().stream()
                        .map(ItemMapper::toItemDtoResponse)
                        .collect(Collectors.toList());
        return RequestDtoResponse.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items)
                .requester(request.getRequester() == null ? null : UserMapper.toUserDtoRequest(request.getRequester()))
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
