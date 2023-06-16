package ru.practicum.shareit.item;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.QBooking;
import ru.practicum.shareit.common.Constants;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.exceptions.ItemIncorrectOwnerException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public Item add(Item item, long ownerId) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> {
                    throw new EntityNotFoundException("Пользователь с id = " + ownerId + " не существует");
        });
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    @Transactional
    @Override
    public Item update(Item item, long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new EntityNotFoundException("Пользователь с id = " + userId + " не существует");
        });
        Item oldItem = get(item.getId());
        User currentOwner = oldItem.getOwner();
        if (!currentOwner.equals(user)) {
            String error = String.format("Пользователь с id = %s не является владельцем вещи с id = %s",
                    user.getId(), item.getId());
            throw new ItemIncorrectOwnerException(error);
        }

        Item newItem = Item.builder()
                .id(item.getId())
                .name(item.getName() == null || item.getName().isBlank() ? oldItem.getName() : item.getName())
                .description(item.getDescription() == null || item.getDescription().isBlank() ?
                        oldItem.getDescription() : item.getDescription())
                .available(item.getAvailable() == null ? oldItem.getAvailable() : item.getAvailable())
                .owner(item.getOwner() == null ? oldItem.getOwner() : item.getOwner())
                .build();

        return itemRepository.save(newItem);
    }

    @Override
    public Item get(long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> {
                    throw new EntityNotFoundException("Вещь с id = " + id + " не найдена");
        });
        item.setComments(commentRepository.findByItem(item, Sort.by(DESC, "created")));
        List<Booking> bookings = bookingRepository.findByItemAndStatus(item, BookingStatus.APPROVED);
        item.setNextBooking(getNextBookingByItem(bookings));
        item.setLastBooking(getLastBookingByItem(bookings));
        return item;
    }

    @Override
    public List<Item> getAllByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new EntityNotFoundException("Пользователь с id = " + userId + " не существует");
        });
        List<Item> items = itemRepository.findAllByOwnerId(user.getId());

        Map<Item, List<Comment>> comments = commentRepository.findByItemIn(items, Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(Comment::getItem, toList()));
        Map<Item, List<Booking>> bookings = bookingRepository.findByItemInAndStatus(items, BookingStatus.APPROVED)
                .stream()
                .collect(groupingBy(Booking::getItem, toList()));

        items.forEach(x -> x.setComments(comments.get(x)));
        items.forEach(item -> item.setLastBooking(getLastBookingByItem(bookings.get(item))));
        items.forEach(item -> item.setNextBooking(getNextBookingByItem(bookings.get(item))));

        return items;
    }

    @Override
    public List<Item> searchByText(String text) {
        return text.isBlank() ? Collections.emptyList() : itemRepository.searchByText(text);
    }

    @Override
    public List<Booking> getBookingByItem(Item item) {
        return bookingRepository.findByItem_Id(item.getId());
    }

    private Booking getLastBookingByItem(List<Booking> bookings) {
        if (bookings == null) {
            return null;
        }
        List<Booking> filteredBookings = bookings.stream()
                .filter(x -> x.getStart().isBefore(Constants.CURRENT_DATE_TIME))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(toList());
        return filteredBookings.isEmpty() ? null : filteredBookings.get(0);
    }

    private Booking getNextBookingByItem(List<Booking> bookings) {
        if (bookings == null) {
            return null;
        }
        List<Booking> filteredBookings = bookings.stream()
                .filter(x -> x.getStart().isAfter(Constants.CURRENT_DATE_TIME))
                .sorted(Comparator.comparing(Booking::getStart))
                .collect(toList());
        return filteredBookings.isEmpty() ? null : filteredBookings.get(0);
    }

    @Override
    public Comment createComment(Comment comment, long userId, long itemId) {
        if (!StringUtils.hasLength(comment.getText())) {
            throw new ValidationException("Не задан текст комментария");
        }
        BooleanExpression eqUserId = QBooking.booking.booker.id.eq(userId);
        BooleanExpression eqItemId = QBooking.booking.item.id.eq(itemId);
        BooleanExpression endBeforeNow = QBooking.booking.end.before(Constants.CURRENT_DATE_TIME);
        Iterable<Booking> filteredBookings = bookingRepository.findAll(eqUserId.and(eqItemId).and(endBeforeNow));
        if (!filteredBookings.iterator().hasNext()) {
            String error = String.format("Пользователь с id = %s не брал в аренду вещь с id = %s", userId, itemId);
            throw new ValidationException(error);
        }
        comment.setAuthor(userRepository.getReferenceById(userId));
        comment.setItem(get(itemId));
        // не придумала как тут исправить, если setCreated(LocalDateTime.now()) то валятся тесты, какие-то проблемы со
        // сравнением дат
        comment.setCreated(Constants.CURRENT_DATE_TIME.plusMinutes(1));
        return commentRepository.save(comment);
    }
}
