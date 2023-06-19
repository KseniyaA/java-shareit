package ru.practicum.shareit.item;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.QBooking;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.exceptions.ItemIncorrectOwnerException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
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
        Item oldItem = get(item.getId(), userId);
        User currentOwner = oldItem.getOwner();
        if (!currentOwner.equals(user)) {
            String error = String.format("Пользователь с id = %s не является владельцем вещи с id = %s",
                    user.getId(), item.getId());
            throw new ItemIncorrectOwnerException(error);
        }
        if (item.getName() != null && !item.getName().isBlank()) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        if (item.getOwner() != null) {
            oldItem.setOwner(item.getOwner());
        }
        return oldItem;
    }

    @Override
    public Item get(long id, long userId) {
        LocalDateTime now = LocalDateTime.now();
        Item item = itemRepository.findById(id).orElseThrow(() -> {
                    throw new EntityNotFoundException("Вещь с id = " + id + " не найдена");
        });
        item.setComments(commentRepository.findByItem(item, Sort.by(DESC, "created")));
        List<Booking> bookings = bookingRepository.findByItemAndStatusOrderByStartAsc(item, BookingStatus.APPROVED);
        item.setNextBooking(item.getOwner().getId() == userId ? getNextBookingByItem(bookings, now) : null);
        item.setLastBooking(item.getOwner().getId() == userId ? getLastBookingByItem(bookings, now) : null);
        return item;
    }

    @Override
    public List<Item> getAllByUser(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new EntityNotFoundException("Пользователь с id = " + userId + " не существует");
        });
        List<Item> items = itemRepository.findAllByOwnerId(user.getId());

        Map<Item, List<Comment>> comments = commentRepository.findByItemIn(items, Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(Comment::getItem, toList()));
        Map<Item, List<Booking>> bookings = bookingRepository.findByItemInAndStatusOrderByStartAsc(items, BookingStatus.APPROVED)
                .stream()
                .collect(groupingBy(Booking::getItem, toList()));

        items.forEach(x -> x.setComments(comments.get(x)));
        items.forEach(item -> item.setLastBooking(getLastBookingByItem(bookings.get(item), now)));
        items.forEach(item -> item.setNextBooking(getNextBookingByItem(bookings.get(item), now)));

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

    private Booking getLastBookingByItem(List<Booking> bookings, LocalDateTime dateTime) {
        if (bookings == null) {
            return null;
        }
        return bookings.stream()
                .filter(x -> !x.getStart().isAfter(dateTime))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private Booking getNextBookingByItem(List<Booking> bookings, LocalDateTime dateTime) {
        if (bookings == null) {
            return null;
        }
        return bookings.stream()
                .filter(x -> x.getStart().isAfter(dateTime))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Comment createComment(Comment comment, long userId, long itemId) {
        final LocalDateTime now = LocalDateTime.now();
        BooleanExpression eqUserId = QBooking.booking.booker.id.eq(userId);
        BooleanExpression eqItemId = QBooking.booking.item.id.eq(itemId);
        BooleanExpression endBeforeNow = QBooking.booking.end.before(now);
        if (!bookingRepository.exists(eqUserId.and(eqItemId).and(endBeforeNow))) {
            String error = String.format("Пользователь с id = %s не брал в аренду вещь с id = %s", userId, itemId);
            throw new ValidationException(error);
        }
        comment.setAuthor(userRepository.getReferenceById(userId));
        comment.setItem(get(itemId, userId));
        comment.setCreated(now);
        return commentRepository.save(comment);
    }
}
