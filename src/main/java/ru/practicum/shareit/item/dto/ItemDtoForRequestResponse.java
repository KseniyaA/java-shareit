package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ItemDtoForRequestResponse {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private long ownerId;
}
