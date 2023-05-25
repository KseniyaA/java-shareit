package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.exception.UserAlreadyExistException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.user.model.User;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserRepositoryImpl implements UserRepository {
    private HashMap<Long, User> users = new HashMap<>();
    private int idSequence = 0;

    @Override
    public User create(User user) {
        EmailValidator validator = EmailValidator.getInstance();
        if (!validator.isValid(user.getEmail())) {
            log.warn("Электронная почта должна содержать символ @");
            throw new ValidationException("Электронная почта должна содержать символ @");
        }
        List<User> usersByEmail = users.values().stream()
                .filter(x -> x.getEmail().equals(user.getEmail()))
                .collect(Collectors.toList());
        if (!usersByEmail.isEmpty()) {
            log.error("Пользователь с email = {} уже создан", user.getEmail());
            throw new UserAlreadyExistException("Пользователь с email = " + user.getEmail() + " уже создан");
        }
        user.setId(++idSequence);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id = {} не найден", user.getId());
            throw new UserNotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        List<User> usersByEmail = users.values().stream()
                .filter(x -> x.getEmail().equals(user.getEmail()) && x.getId() != user.getId())
                .collect(Collectors.toList());
        if (!usersByEmail.isEmpty()) {
            log.error("Пользователь с email = {} уже создан", user.getEmail());
            throw new UserAlreadyExistException("Пользователь с email = " + user.getEmail() + " уже создан");
        }
        User oldUser = users.get(user.getId());
        User newUser = User.builder()
                .id(user.getId())
                .name(Optional.ofNullable(user.getName()).orElse(oldUser.getName()))
                .email(Optional.ofNullable(user.getEmail()).orElse(oldUser.getEmail())).build();
        users.put(user.getId(), newUser);
        return users.get(user.getId());
    }

    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }

    @Override
    public User getById(Long id) {
        if (!users.containsKey(id)) {
            log.error("Пользователь с id = {} не найден", id);
            throw new UserNotFoundException("Пользователь с id = " + id + " не найден");
        }
        return users.get(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }
}
