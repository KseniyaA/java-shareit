package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item add(Item item, long ownerId) {
        return itemRepository.add(item, userRepository.getById(ownerId));
    }

    @Override
    public Item update(Item item, long userId) {
        User user = userRepository.getById(userId);
        return itemRepository.update(item, user);
    }

    @Override
    public Item get(long id) {
        return itemRepository.get(id);
    }

    @Override
    public List<Item> getAllByUser(Long userId) {
        User user = userRepository.getById(userId);
        return itemRepository.getAllByUser(user);
    }

    @Override
    public List<Item> searchByText(String text) {
        return itemRepository.searchByText(text);
    }
}
