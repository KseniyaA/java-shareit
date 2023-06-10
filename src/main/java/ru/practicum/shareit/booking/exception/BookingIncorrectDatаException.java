package ru.practicum.shareit.booking.exception;

public class BookingIncorrectDatаException extends RuntimeException {
    public BookingIncorrectDatаException(String message) {
        super(message);
    }
}
