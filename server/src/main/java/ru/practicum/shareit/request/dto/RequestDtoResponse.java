package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Getter;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoRequest;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RequestDtoResponse {
    private long id;

    private String description;

    private LocalDateTime created;

    private List<ItemDtoResponse> items;

    private UserDtoRequest requester;
}
