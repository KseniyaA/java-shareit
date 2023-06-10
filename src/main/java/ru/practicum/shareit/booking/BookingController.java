package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {
    private final BookingService bookingService;
    private final UserService userService;

    @Autowired
    public BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PostMapping
    public BookingDtoResponse add(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Valid @RequestBody BookingDtoRequest bookingDto) {
        User booker = userService.getById(userId);
        Booking createdBooking = bookingService.create(BookingMapper.toBookingCreateRequest(bookingDto), booker);
        return BookingMapper.toBookingDtoResponse(createdBooking);
    }

    /**
     * Подтверждение или отклонение запроса на бронирование
     * PATCH /bookings/{bookingId}?approved={approved}
     * */
    @PatchMapping("/{bookingId}")
    public BookingDtoResponse approve(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable("bookingId") long bookingId,
                                  @RequestParam(defaultValue = "") Boolean approved) {
        return BookingMapper.toBookingDtoResponse(bookingService.approve(bookingId, approved, userId));
    }

    /**
     * Получение данных о конкретном бронировании
     * GET /bookings/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public BookingDtoResponse get(@RequestHeader("X-Sharer-User-Id") long userId,
                        @PathVariable("bookingId") long bookingId) {
        return BookingMapper.toBookingDtoResponse(bookingService.get(bookingId, userId));
    }

    /**
     * Получение списка всех бронирований текущего пользователя
     * GET /bookings?state={state}
     */
    @GetMapping
    public List<BookingDtoResponse> getAllBookingsByUser(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(defaultValue = "ALL") String state) {
        userService.getById(userId);
        return BookingMapper.toBookingDtoResponseList(bookingService.getAllBookingsByUser(userId, state));
    }

    /**
     * Получение списка бронирований для всех вещей текущего пользователя
     * GET /bookings/owner?state={state}
     */
    @GetMapping("/owner")
    public List<BookingDtoResponse> getAllBookingsByItemOwner(@RequestHeader("X-Sharer-User-Id") long itemOwnerId,
                                                 @RequestParam(defaultValue = "ALL") String state) {
        userService.getById(itemOwnerId);
        return BookingMapper.toBookingDtoResponseList(bookingService.getAllBookingsByItemOwner(itemOwnerId, state));
    }

}
