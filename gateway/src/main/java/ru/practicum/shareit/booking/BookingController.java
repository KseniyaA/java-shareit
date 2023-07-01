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
	private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
	private static final String BOOKING_ID = "bookingId";
	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> add(@RequestHeader(X_SHARER_USER_ID) long userId,
									  @RequestBody @Valid BookingDtoRequest bookingDto) {
		log.info("Получен запрос POST /bookings с параметрами userId = {}, dto = {}", userId, bookingDto);
		return bookingClient.create(bookingDto, userId);
	}

	/**
	 * Подтверждение или отклонение запроса на бронирование
	 * PATCH /bookings/{bookingId}?approved={approved}
	 * */
	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approve(@RequestHeader(X_SHARER_USER_ID) long userId,
										  @PathVariable(BOOKING_ID) long bookingId,
										  @RequestParam Boolean approved) {
		log.info("Получен запрос PATCH /bookings/bookingId с параметрами userId = {}, bookingId = {}, approved = {}",
				userId, bookingId, approved);
		return bookingClient.approve(bookingId, approved, userId);
	}

	/**
	 * Получение данных о конкретном бронировании
	 * GET /bookings/{bookingId}
	 */
	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> get(@RequestHeader(X_SHARER_USER_ID) long userId,
									  @PathVariable(BOOKING_ID) long bookingId) {
		log.info("Получен запрос GET /bookings/bookingId с параметрами userId = {}, bookingId = {}", userId, bookingId);
		return bookingClient.getById(bookingId, userId);
	}

	/**
	 * Получение списка всех бронирований текущего пользователя
	 * GET /bookings?state={state}
	 */
	@GetMapping
	public ResponseEntity<Object> getAllBookingsByUser(@RequestHeader(X_SHARER_USER_ID) long userId,
													   @RequestParam(defaultValue = "ALL") String state,
													   @RequestParam(required = false) @ValidateFromIfPresent Integer from,
													   @RequestParam(required = false) @ValidateSizeIfPresent Integer size) {
		log.info("Получен запрос GET /bookings с параметрами userId = {}, state = {}, from = {}, size = {}",
				userId, state, from, size);
		return bookingClient.getAllBookingsByUser(userId, state, from, size);
	}

	/**
	 * Получение списка бронирований для всех вещей текущего пользователя
	 * GET /bookings/owner?state={state}
	 */
	@GetMapping("/owner")
	public ResponseEntity<Object> getAllBookingsByItemOwner(@RequestHeader(X_SHARER_USER_ID) long itemOwnerId,
															@RequestParam(defaultValue = "ALL") String state,
															@RequestParam(required = false) @ValidateFromIfPresent Integer from,
															@RequestParam(required = false) @ValidateSizeIfPresent Integer size) {
		log.info("Получен запрос GET /bookings/owner с параметрами userId = {}, state = {}, from = {}, size = {}",
				itemOwnerId, state, from, size);
		return bookingClient.getAllBookingsByItemOwner(itemOwnerId, state, from, size);
	}

}
