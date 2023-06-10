package ru.practicum.shareit.booking;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.exception.BookingIncorrectDatаException;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.BookingUnavailableOperationException;
import ru.practicum.shareit.booking.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.QItem;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;

    private void checkBookingDates(Booking booking) {
        LocalDateTime start = booking.getStart();
        LocalDateTime end = booking.getEnd();
        if (start == null || end == null) {
            throw new BookingIncorrectDatаException("Дата начала и окончания бронирования должны быть заданы");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new BookingIncorrectDatаException("Дата начала бронирования не может быть раньше текущего дня");
        }
        if (end.isBefore(LocalDateTime.now())) {
            throw new BookingIncorrectDatаException("Дата окончания бронирования не может быть в прошлом");
        }
        if (start.isAfter(end)) {
            throw new BookingIncorrectDatаException("Дата окончания бронирования не может быть раньше даты начала");
        }
        if (start.equals(end)) {
            throw new BookingIncorrectDatаException("Дата начала и окончания бронирования не могут совпадать");
        }

    }

    @Transactional
    @Override
    public Booking create(Booking booking, User booker) {
        Optional<Item> itemOptional = itemRepository.findById(booking.getItem().getId());
        if (itemOptional.isEmpty()) {
            throw new ItemNotFoundException("Вещь с id = " + booking.getItem().getId() + " не найдена");
        }
        if (booker.getId().equals(itemOptional.get().getOwner().getId())) {
            throw new ItemNotFoundException("Владелец вещи не может забронировать вещь");
        }
        if (!itemOptional.get().getAvailable()) {
            throw new BookingIncorrectDatаException("Вещь с id = " + booking.getItem().getId() + " не доступна для бронирования");
        }
        checkBookingDates(booking);
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(booker);
        booking.setItem(itemOptional.get());
        return bookingRepository.save(booking);
    }

    @Transactional
    @Override
    public Booking update(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public Booking getById(Long id) {
        Optional<Booking> bookingOptional = bookingRepository.findById(id);
        if (bookingOptional.isEmpty()) {
            throw new BookingNotFoundException("Бронирование c id = " + id + " не найдено");
        }
        return bookingOptional.get();
    }

    @Transactional
    @Override
    public Booking approve(long bookingId, Boolean approved, long userId) {
        if (approved == null) {
            throw new BookingIncorrectDatаException("Не передано значение параметра approve");
        }
        Booking booking = getById(bookingId);
        if (booking.getItem().getOwner().getId() != userId) {
            throw new BookingUnavailableOperationException("Подтверждение или отклонение запроса может быть выполнено " +
                    "только владельцем вещи");
        }
        if (booking.getStatus().equals(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED)) {
            throw new BookingIncorrectDatаException("Статус уже изменен");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return booking;
    }

    @Override
    public Booking get(long bookingId, long userId) {
        Booking booking = getById(bookingId);
        if (!(userId == booking.getBooker().getId() || userId == booking.getItem().getOwner().getId())) {
            throw new BookingUnavailableOperationException("Получение данных о бронировании может быть выполнено " +
                    "либо автором бронирования, либо владельцем вещи");
        }
        return booking;
    }

    @Override
    public List<Booking> getAllBookingsByUser(long userId, String state) {
        BooleanExpression byBookerId = QBooking.booking.booker.id.eq(userId);
        return getBookingsByParams(state, byBookerId);
    }

    @Override
    public List<Booking> getAllBookingsByItemOwner(long itemOwnerId, String state) {
        BooleanExpression byItemOwnerId = QItem.item.owner.id.eq(itemOwnerId);
        return getBookingsByParams(state, byItemOwnerId);
    }

    private List<Booking> getBookingsByParams(String state, BooleanExpression byOwnerOrBookerId) {
        Sort sortByStartDateDesc = Sort.by("start").descending();
        Sort sortByStartDateAsc = Sort.by("start").ascending();
        switch (state.toUpperCase()) {
            case "ALL":
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId, sortByStartDateDesc);
            case "CURRENT":
                BooleanExpression byEndBefore = QBooking.booking.end.after(LocalDateTime.now());
                BooleanExpression byStartAfter = QBooking.booking.start.before(LocalDateTime.now());
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId.and(byEndBefore).and(byStartAfter)
                        , sortByStartDateAsc);
            case "PAST":
                BooleanExpression byEndAfter = QBooking.booking.end.before(LocalDateTime.now());
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId.and(byEndAfter), sortByStartDateDesc);
            case "FUTURE":
                byStartAfter = QBooking.booking.start.after(LocalDateTime.now());
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId.and(byStartAfter), sortByStartDateDesc);
            case "WAITING":
                BooleanExpression eqWaitingStatus = QBooking.booking.status.eq(BookingStatus.WAITING);
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId.and(eqWaitingStatus), sortByStartDateDesc);
            case "REJECTED":
                BooleanExpression eqRejectedStatus = QBooking.booking.status.eq(BookingStatus.REJECTED);
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId.and(eqRejectedStatus), sortByStartDateDesc);
            default:
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
    }
}
