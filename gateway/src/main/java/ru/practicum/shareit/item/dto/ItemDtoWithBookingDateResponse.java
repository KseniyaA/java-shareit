package ru.practicum.shareit.item.dto;


import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.request.dto.RequestDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoResponse;

import java.util.List;

@Data
@Builder
public class ItemDtoWithBookingDateResponse {
    private long id;
    private String name;
    private String description;
    private UserDtoResponse owner;
    private Boolean available;
    private RequestDtoResponse request;
    private BookingSimpleDto lastBooking;
    private BookingSimpleDto nextBooking;
    private List<CommentDtoResponse> comments;
}
