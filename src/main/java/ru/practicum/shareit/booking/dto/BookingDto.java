package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.item.model.Item;

import java.time.LocalDate;

public class BookingDto {
    private Item item;
    private LocalDate dateFrom;
    private LocalDate dateTill;
}
