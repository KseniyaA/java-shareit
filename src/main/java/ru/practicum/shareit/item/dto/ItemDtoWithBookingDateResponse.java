package ru.practicum.shareit.item.dto;


import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Data
@Builder
public class ItemDtoWithBookingDateResponse {
    private long id;
    private String name;
    private String description;
    private UserDto owner;
    private Boolean available;
    private RequestDto request;
    private BookingSimpleDto lastBooking;
    private BookingSimpleDto nextBooking;
    private List<CommentDto> comments;
}
