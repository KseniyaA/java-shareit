package ru.practicum.shareit.item.exceptions;

public class ItemIncorrectOwnerException extends RuntimeException {
    public ItemIncorrectOwnerException(String message) {
        super(message);
    }
}
