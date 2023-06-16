package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item add(Item item, long ownerId);

    Item update(Item item, long userId);

    Item get(long id, long userId);

    List<Item> getAllByUser(Long userId);

    List<Item> searchByText(String text);

    List<Booking> getBookingByItem(Item item);

    Comment createComment(Comment toComment, long userId, long itemId);
}
