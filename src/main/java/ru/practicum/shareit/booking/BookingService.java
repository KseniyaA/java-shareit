package ru.practicum.shareit.booking;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface BookingService {
    Booking create(Booking booking, User booker);

    Booking update(Booking booking);

    void deleteById(Long id);

    Booking getById(Long id);

    Booking approve(long bookingId, Boolean approved, long userId);

    Booking get(long bookingId, long userId);

    List<Booking> getAllBookingsByUser(long userId, String state);

    List<Booking> getAllBookingsByItemOwner(long itemOwnerId, String state);
}
