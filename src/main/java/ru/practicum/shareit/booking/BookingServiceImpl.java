package ru.practicum.shareit.booking;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.exception.BookingIncorrectDataException;
import ru.practicum.shareit.booking.exception.BookingUnavailableOperationException;
import ru.practicum.shareit.booking.exception.UnsupportedStatusException;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.QItem;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static ru.practicum.shareit.common.Constants.CURRENT_DATE_TIME;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    public static final String START_FIELD = "start";
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public Booking create(Booking booking, User booker) {
        Item item = itemRepository.findById(booking.getItem().getId()).orElseThrow(() -> {
            throw new EntityNotFoundException("Вещь с id = " + booking.getItem().getId() + " не найдена");
        });
        if (booker.getId().equals(item.getOwner().getId())) {
            throw new EntityNotFoundException("Владелец вещи не может забронировать вещь");
        }
        if (!item.getAvailable()) {
            throw new BookingIncorrectDataException("Вещь с id = " + booking.getItem().getId() + " не доступна для бронирования");
        }
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(booker);
        booking.setItem(item);
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
        return bookingRepository.findById(id)
                .orElseThrow(() -> {
                    throw new EntityNotFoundException("Бронирование c id = " + id + " не найдено");
                });
    }

    @Transactional
    @Override
    public Booking approve(long bookingId, Boolean approved, long userId) {
        Booking booking = getById(bookingId);
        if (booking.getItem().getOwner().getId() != userId) {
            throw new BookingUnavailableOperationException("Подтверждение или отклонение запроса может быть выполнено " +
                    "только владельцем вещи");
        }
        if (booking.getStatus().equals(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED)) {
            throw new BookingIncorrectDataException("Статус уже изменен");
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
        Sort sortByStartDateDesc = Sort.by(START_FIELD).descending();
        Sort sortByStartDateAsc = Sort.by(START_FIELD).ascending();
        BookingFilterState filterState = BookingFilterState.findByValue(state);
        if (filterState == null) {
            throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        switch (filterState) {
            case ALL:
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId, sortByStartDateDesc);
            case CURRENT:
                BooleanExpression byEndBefore = QBooking.booking.end.after(CURRENT_DATE_TIME);
                BooleanExpression byStartAfter = QBooking.booking.start.before(CURRENT_DATE_TIME);
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId.and(byEndBefore).and(byStartAfter),
                        sortByStartDateAsc);
            case PAST:
                BooleanExpression byEndAfter = QBooking.booking.end.before(CURRENT_DATE_TIME);
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId.and(byEndAfter), sortByStartDateDesc);
            case FUTURE:
                byStartAfter = QBooking.booking.start.after(CURRENT_DATE_TIME);
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId.and(byStartAfter), sortByStartDateDesc);
            case WAITING:
                BooleanExpression eqWaitingStatus = QBooking.booking.status.eq(BookingStatus.WAITING);
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId.and(eqWaitingStatus), sortByStartDateDesc);
            case REJECTED:
                BooleanExpression eqRejectedStatus = QBooking.booking.status.eq(BookingStatus.REJECTED);
                return (List<Booking>) bookingRepository.findAll(byOwnerOrBookerId.and(eqRejectedStatus), sortByStartDateDesc);
            default:
                throw new UnsupportedStatusException("Unknown state");
        }
    }
}
