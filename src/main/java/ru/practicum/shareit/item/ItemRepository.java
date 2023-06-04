package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRepository {
    Item add(Item item, User owner);

    Item update(Item item, User user);

    Item get(long id);

    List<Item> getAllByUser(User user);

    List<Item> searchByText(String text);
}
