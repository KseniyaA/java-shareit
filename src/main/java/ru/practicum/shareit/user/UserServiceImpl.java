package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        User oldUser = getById(user.getId());
        User newUser = new User();
        newUser.setId(user.getId());
        newUser.setName(user.getName() == null || user.getName().isBlank() ? oldUser.getName() : user.getName());
        newUser.setEmail(user.getEmail() == null || user.getEmail().isBlank() ? oldUser.getEmail() : user.getEmail());
        return userRepository.save(newUser);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    throw new EntityNotFoundException("Пользователь с id = " + id + " не найден");
                });
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }
}
