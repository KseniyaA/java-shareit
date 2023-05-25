package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.exceptions.ItemIncorrectOwnerException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ItemRepositoryImpl implements ItemRepository {
    private HashMap<Long, Item> items = new HashMap<>();
    private int idSequence = 0;

    @Override
    public Item add(Item item, User owner) {
        item.setId(++idSequence);
        item.setOwner(owner);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item, User user) {
        Item oldItem = items.get(item.getId());
        if (!items.containsKey(item.getId())) {
            log.error("Вещь с id = {} не найден", item.getId());
            throw new ItemNotFoundException("Вещь с id = " + item.getId() + " не найдена");
        }
        if (!oldItem.getOwner().equals(user)) {
            log.error("Пользователь с id = {} не является владельцем вещи с id = {}", user.getId(), item.getId());
            throw new ItemIncorrectOwnerException("Пользователь с id = " + user.getId() + " не является владельцем " +
                    "вещи с id = " + item.getId());
        }
        Item newItem = Item.builder().
                id(item.getId()).
                name(Optional.ofNullable(item.getName()).orElse(oldItem.getName())).
                description(Optional.ofNullable(item.getDescription()).orElse(oldItem.getDescription())).
                available(Optional.ofNullable(item.getAvailable()).orElse(oldItem.getAvailable())).
                owner(Optional.ofNullable(item.getOwner()).orElse(oldItem.getOwner())).build();
        items.put(item.getId(), newItem);
        return items.get(item.getId());
    }

    @Override
    public Item get(long id) {
        return items.get(id);
    }

    @Override
    public List<Item> getAllByUser(User user) {
        return items.values().stream().filter(x -> x.getOwner().equals(user)).collect(Collectors.toList());
    }

    @Override
    public List<Item> searchByText(String text) {
        if (text.isBlank() || text.isEmpty()) {
            return List.of();
        }
        return items.values().stream()
                .filter(x -> x.getName().toLowerCase().contains(text.toLowerCase())
                        || x.getDescription().toLowerCase().contains(text.toLowerCase()))
                .filter(x -> x.getAvailable().equals(Boolean.TRUE))
                .distinct()
                .collect(Collectors.toList());
    }
}
