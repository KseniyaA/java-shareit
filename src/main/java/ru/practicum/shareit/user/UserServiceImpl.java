package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public User create(User user) {
        EmailValidator validator = EmailValidator.getInstance();
        if (!validator.isValid(user.getEmail())) {
            log.warn("Электронная почта должна содержать символ @");
            throw new ValidationException("Электронная почта должна содержать символ @");
        }
        /*List<User> usersByEmail = userRepository.findByEmail(user.getEmail());
        if (!usersByEmail.isEmpty()) {
            log.error("Пользователь с email = {} уже создан", user.getEmail());
            throw new UserAlreadyExistException("Пользователь с email = " + user.getEmail() + " уже создан");
        }*/
        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        /*List<User> usersByEmail = userRepository.findByEmail(user.getEmail());
        if (!usersByEmail.isEmpty()) {
            log.error("Пользователь с email = {} уже создан", user.getEmail());
            throw new UserAlreadyExistException("Пользователь с email = " + user.getEmail() + " уже создан");
        }*/
        User oldUser = getById(user.getId());
        User newUser = new User();
        newUser.setId(user.getId());
        newUser.setName(Optional.ofNullable(user.getName()).orElse(oldUser.getName()));
        newUser.setEmail(Optional.ofNullable(user.getEmail()).orElse(oldUser.getEmail()));

        return userRepository.save(newUser);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User getById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
                throw new UserNotFoundException("Пользователь с id = " + id + " не найден");
        }
        return userOptional.get();
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }
}
