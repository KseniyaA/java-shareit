package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingSimpleDto {
    private Long id;
    private Long itemId;
    private Long bookerId;
    private String start;
    private String end;
}
