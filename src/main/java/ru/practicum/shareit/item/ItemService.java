package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item add(Item item, long ownerId);

    Item update(Item item, long userId);

    Item get(long id);

    List<Item> getAllByUser(Long userId);

    List<Item> searchByText(String text);
}
