package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    private User makeUser() {
        return User.builder()
                .name("name")
                .email("name@ya.ru")
                .build();
    }

    private User makeUpdatedUser() {
        return User.builder()
                .name("updatedName")
                .email("updatedName@ya.ru")
                .build();
    }

    @Test
    void createUserWithSuccessTest() {
        UserService userService = new UserServiceImpl(userRepository);

        User userIn = makeUser();
        when(userRepository.save(userIn)).thenReturn(userIn);

        User createdUser = userService.create(userIn);

        assertThat(createdUser.getName(), equalTo("name"));
        assertThat(createdUser.getEmail(), equalTo("name@ya.ru"));

        verify(userRepository, times(1)).save(Mockito.any());
    }

    @Test
    void deleteById() {
        UserService userService = new UserServiceImpl(userRepository);

        userService.deleteById(1L);

        verify(userRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void getByIdSuccessTest() {
        UserService userService = new UserServiceImpl(userRepository);
        User userIn = makeUser();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userIn));

        User userById = userService.getById(1L);

        assertThat(userById.getName(), equalTo("name"));
        assertThat(userById.getEmail(), equalTo("name@ya.ru"));
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void getByIdExceptionTest() {
        UserService userService = new UserServiceImpl(userRepository);

        when(userRepository.findById(anyLong())).thenThrow(new EntityNotFoundException("Пользователь не найден"));

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> userService.getById(1L));

        Assertions.assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void getAll() {
        UserService userService = new UserServiceImpl(userRepository);

        when(userRepository.findAll()).thenReturn(Arrays.asList(makeUser(), makeUpdatedUser()));

        List<User> all = userService.getAll();

        verify(userRepository, times(1)).findAll();
        assertThat(all.size(), equalTo(2));
    }
}