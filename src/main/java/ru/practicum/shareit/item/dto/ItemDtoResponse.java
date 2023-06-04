package ru.practicum.shareit.item.dto;


import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

@Data
@Builder
public class ItemDtoResponse {
    private long id;
    private String name;
    private String description;
    private UserDto owner;
    private Boolean available;
    private ItemRequestDto request;
}
