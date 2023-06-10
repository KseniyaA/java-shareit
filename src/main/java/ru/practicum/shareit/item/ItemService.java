package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item add(Item item, long ownerId);

    Item update(Item item, long userId);

    Item get(long id);

    List<Item> getAllByUser(Long userId);

    List<Item> searchByText(String text);

    List<Booking> getBookingByItem(Item item);

    Booking getLastBookingByItem(List<Booking> items);

    Booking getNextBookingByItem(List<Booking> items);

    Comment createComment(Comment toComment, long userId, long itemId);

    List<Comment> getComments(long itemId);
}
