package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BookingMapper {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static Booking toBookingCreateRequest(BookingDtoRequest bookingDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        LocalDateTime startDate = bookingDto.getStart() == null ? null :
                LocalDateTime.parse(bookingDto.getStart(), formatter);
        LocalDateTime endDate = bookingDto.getEnd() == null ? null :
                LocalDateTime.parse(bookingDto.getEnd(), formatter);

        return Booking.builder()
                .start(startDate)
                .end(endDate)
                .item(ItemMapper.toItemBookingCreateRequest(bookingDto.getItemId()))
                .build();
    }

    public static BookingDtoResponse toBookingDtoResponse(Booking booking) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
                .withZone(ZoneId.of("GMT"));

        String startDate = formatter.format(booking.getStart());
        String endDate = formatter.format(booking.getEnd());

        return BookingDtoResponse.builder()
                .id(booking.getId())
                .start(startDate)
                .end(endDate)
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .item(ItemMapper.toItemDtoResponse(booking.getItem()))
                .status(booking.getStatus().name())
                .build();
    }

    public static List<BookingDtoResponse> toBookingDtoResponseList(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toBookingDtoResponse)
                .collect(Collectors.toList());
    }
}
