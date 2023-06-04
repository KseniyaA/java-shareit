package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User create(User user);

    User update(User user);

    void deleteById(Long id);

    User getById(Long id);

    List<User> getAll();
}
