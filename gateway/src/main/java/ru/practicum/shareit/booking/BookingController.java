package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.common.ValidateFromIfPresent;
import ru.practicum.shareit.common.ValidateSizeIfPresent;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> add(@RequestHeader("X-Sharer-User-Id") long userId,
								  @RequestBody @Valid BookingDtoRequest bookingDto) {
		return bookingClient.create(bookingDto, userId);
	}

	/**
	 * Подтверждение или отклонение запроса на бронирование
	 * PATCH /bookings/{bookingId}?approved={approved}
	 * */
	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approve(@RequestHeader("X-Sharer-User-Id") long userId,
									  @PathVariable("bookingId") long bookingId,
									  @RequestParam Boolean approved) {
		return bookingClient.approve(bookingId, approved, userId);
	}

	/**
	 * Получение данных о конкретном бронировании
	 * GET /bookings/{bookingId}
	 */
	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> get(@RequestHeader("X-Sharer-User-Id") long userId,
									  @PathVariable("bookingId") long bookingId) {
		return bookingClient.getById(bookingId, userId);
	}

	/**
	 * Получение списка всех бронирований текущего пользователя
	 * GET /bookings?state={state}
	 */
	@GetMapping
	public ResponseEntity<Object> getAllBookingsByUser(@RequestHeader("X-Sharer-User-Id") long userId,
													   @RequestParam(defaultValue = "ALL") String state,
													   @RequestParam(required = false) @ValidateFromIfPresent Integer from,
													   @RequestParam(required = false) @ValidateSizeIfPresent Integer size
	) {
		return bookingClient.getAllBookingsByUser(userId, state, from, size);
	}

	/**
	 * Получение списка бронирований для всех вещей текущего пользователя
	 * GET /bookings/owner?state={state}
	 */
	@GetMapping("/owner")
	public ResponseEntity<Object> getAllBookingsByItemOwner(@RequestHeader("X-Sharer-User-Id") long itemOwnerId,
															@RequestParam(defaultValue = "ALL") String state,
															@RequestParam(required = false) @ValidateFromIfPresent Integer from,
															@RequestParam(required = false) @ValidateSizeIfPresent Integer size) {
		return bookingClient.getAllBookingsByItemOwner(itemOwnerId, state, from, size);
	}

}
