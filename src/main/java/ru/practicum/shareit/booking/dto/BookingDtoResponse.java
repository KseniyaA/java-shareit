package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.user.dto.UserDto;

@Data
@Builder
public class BookingDtoResponse {
    private Long id;
    private String start;
    private String end;
    private ItemDtoResponse item;
    private UserDto booker;
    private String status;
}
