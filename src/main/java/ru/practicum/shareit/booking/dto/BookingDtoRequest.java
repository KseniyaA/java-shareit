package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.valid.StartBeforeEndDateValid;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@StartBeforeEndDateValid
public class BookingDtoRequest {
    @FutureOrPresent(message = "Start date must be present or future")
    private LocalDateTime start;

    private LocalDateTime end;

    @NotNull
    private Long itemId;
}
