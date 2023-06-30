package ru.practicum.shareit.booking.exception;

public class BookingUnavailableOperationException extends RuntimeException {
    public BookingUnavailableOperationException(String message) {
        super(message);
    }
}
