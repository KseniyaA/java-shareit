package ru.practicum.shareit.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.item.exceptions.ItemIncorrectOwnerException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.user.exception.UserAlreadyExistException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(value = {UserNotFoundException.class, ItemNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(final RuntimeException e) {
        return new ErrorResponse(
                "Ошибка получения по id", e.getMessage()
        );
    }

    @ExceptionHandler(value = {UserAlreadyExistException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserException(final UserAlreadyExistException e) {
        return new ErrorResponse(
                "Ошибка получения по id", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectParameterException(final ValidationException e) {
        return new ErrorResponse(
                "Ошибка валидации", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleItemIncorrectOwnerException(final ItemIncorrectOwnerException e) {
        return new ErrorResponse(
                "Ошибка валидации", e.getMessage()
        );
    }
}
