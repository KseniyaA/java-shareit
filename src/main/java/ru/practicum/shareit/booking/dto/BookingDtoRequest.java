package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingDtoRequest {
    private String start;
    private String end;
    private Long itemId;
}
