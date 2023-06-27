package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDtoForRequestResponse;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RequestDtoResponse {
    private long id;

    private String description;

    private LocalDateTime created;

    private List<ItemDtoForRequestResponse> items;
}
