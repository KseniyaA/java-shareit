package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.dto.UserDtoRequest;

import java.util.List;

@Data
@Builder
public class ItemDtoResponse {
    private long id;
    private String name;
    private String description;
    private UserDtoRequest owner;
    private Boolean available;
    private Long requestId;
    private List<CommentDtoResponse> comments;
}
