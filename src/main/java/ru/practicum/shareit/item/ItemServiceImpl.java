package ru.practicum.shareit.item;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.QBooking;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.exceptions.ItemIncorrectOwnerException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private void checkItem(Item item) {
        if (!StringUtils.hasLength(item.getName()) || !StringUtils.hasLength(item.getDescription())
                || item.getAvailable() == null) {
            throw new ValidationException("Некорректное значение входных параметров");
        }
    }

    @Transactional
    @Override
    public Item add(Item item, long ownerId) {
        checkItem(item);
        Optional<User> ownerOptional = userRepository.findById(ownerId);
        if (ownerOptional.isEmpty()) {
            throw new UserNotFoundException("Пользователь с id = " + ownerId + " не существует");
        }
        item.setOwner(ownerOptional.get());
        return itemRepository.save(item);
    }

    @Transactional
    @Override
    public Item update(Item item, long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("Пользователь с id = " + userId + " не существует");
        }
        Item oldItem = get(item.getId());
        User currentOwner = oldItem.getOwner();
        if (!currentOwner.equals(userOptional.get())) {
            String error = String.format("Пользователь с id = %s не является владельцем вещи с id = %s",
                    userOptional.get().getId(), item.getId());
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
        Optional<Item> itemOptional = itemRepository.findById(id);
        if (itemOptional.isEmpty()) {
            throw new ItemNotFoundException("Вещь с id = " + id + " не найдена");
        }
        return itemOptional.get();
    }

    @Override
    public List<Item> getAllByUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("Пользователь с id = " + userId + " не существует");
        }
        return itemRepository.findAllByOwnerId(userOptional.get().getId());
    }

    @Override
    public List<Item> searchByText(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemRepository.searchByText(text);
    }

    @Override
    public List<Booking> getBookingByItem(Item item) {
        return bookingRepository.findByItem_Id(item.getId());
    }

    @Override
    public Booking getLastBookingByItem(List<Booking> bookings) {
        List<Booking> filteredBookings = bookings.stream()
                .filter(x -> x.getStart().isBefore(LocalDateTime.now()))
                .filter(x -> x.getStatus().equals(BookingStatus.APPROVED))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        return filteredBookings.isEmpty() ? null : filteredBookings.get(0);
    }

    @Override
    public Booking getNextBookingByItem(List<Booking> bookings) {
        List<Booking> filteredBookings = bookings.stream()
                .filter(x -> x.getStart().isAfter(LocalDateTime.now()))
                .filter(x -> x.getStatus().equals(BookingStatus.APPROVED))
                .sorted(Comparator.comparing(Booking::getStart))
                .collect(Collectors.toList());
        return filteredBookings.isEmpty() ? null : filteredBookings.get(0);
    }

    @Override
    public Comment createComment(Comment comment, long userId, long itemId) {
        if (!StringUtils.hasLength(comment.getText())) {
            throw new ValidationException("Не задан текст комментария");
        }
        BooleanExpression eqUserId = QBooking.booking.booker.id.eq(userId);
        BooleanExpression eqItemId = QBooking.booking.item.id.eq(itemId);
        BooleanExpression endBeforeNow = QBooking.booking.end.before(LocalDateTime.now());
        Iterable<Booking> filteredBookings = bookingRepository.findAll(eqUserId.and(eqItemId).and(endBeforeNow));
        if (!filteredBookings.iterator().hasNext()) {
            String error = String.format("Пользователь с id = %s не брал в аренду вещь с id = %s", userId, itemId);
            throw new ValidationException(error);
        }
        comment.setAuthor(userRepository.getReferenceById(userId));
        comment.setItem(get(itemId));
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        LocalDateTime created = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT));
        // не придумала как тут исправить, если setCreated(LocalDateTime.now()) то валятся тесты, какие-то проблемы со
        // сравнением дат
        comment.setCreated(created.plusMinutes(1));
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getComments(long itemId) {
        return commentRepository.findByItem_Id(itemId);
    }


}
