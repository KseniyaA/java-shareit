package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.dto.UserDto;

@Data
@Builder
public class RequestDto {
    private long id;
    private String description;
    private UserDto requester;
}
