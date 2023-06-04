package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
public class ItemRequestDto {
    private long id;
    private String description;
    private UserDto requester;
    private LocalDateTime created;
}
