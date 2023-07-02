package ru.practicum.shareit.booking.exception;

public class BookingIncorrectDataException extends RuntimeException {
    public BookingIncorrectDataException(String message) {
        super(message);
    }
}
