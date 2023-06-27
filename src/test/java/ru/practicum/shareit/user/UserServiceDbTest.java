package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceDbTest {
    private final EntityManager em;
    private final UserService userService;

    private User makeUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }

    private User makeUser(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }

    @BeforeEach
    private void clear() {
        em.createQuery("delete from User").executeUpdate();
    }

    @Test
    void createUserTest() {
        User userDto = makeUser("name", "name@ya.ru");
        User createdUser = userService.create(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User createdUserFromDb = query
                .setParameter("email", createdUser.getEmail())
                .getSingleResult();

        assertThat(createdUser.getId(), notNullValue());
        assertThat(createdUser.getName(), equalTo(createdUserFromDb.getName()));
    }

    @Test
    void updateUserTest() {
        User userIn = makeUser("name", "name@ya.ru");
        User createdUser = userService.create(userIn);
        User updateDto = makeUser(createdUser.getId(), "updatedName", "updatedName@ya.ru");

        User updatedUser = userService.update(updateDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User updatedUserFromDb = query
                .setParameter("id", createdUser.getId())
                .getSingleResult();

        assertThat(updatedUser.getName(), equalTo(updatedUserFromDb.getName()));
        assertThat(createdUser.getEmail(), equalTo(updatedUserFromDb.getEmail()));
    }

    @Test
    void deleteUserTest() {
        User userIn = makeUser("name", "name@ya.ru");
        User createdUser = userService.create(userIn);

        userService.deleteById(createdUser.getId());

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        List<User> userByIdFromDb = query
                .setParameter("id", createdUser.getId())
                .getResultList();

        assertThat(userByIdFromDb.size(), equalTo(0));
    }

    @Test
    void getUserSuccessTest() {
        User userIn = makeUser("name", "name@ya.ru");
        User createdUser = userService.create(userIn);

        User userById = userService.getById(createdUser.getId());

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User userByIdFromDb = query
                .setParameter("id", createdUser.getId())
                .getSingleResult();

        assertThat(userByIdFromDb.getId(), equalTo(userById.getId()));
        assertThat(userByIdFromDb.getName(), equalTo(userById.getName()));
        assertThat(userByIdFromDb.getEmail(), equalTo(userById.getEmail()));
    }

    @Test
    void getAllSuccessTest() {
        userService.create(makeUser("name", "name@ya.ru"));
        userService.create(makeUser("name2", "name2@ya.ru"));

        List<User> allUsers = userService.getAll();

        List<User> usersFromDb = em.createQuery("Select u from User u").getResultList();

        assertThat(usersFromDb.size(), equalTo(allUsers.size()));
        assertThat(usersFromDb.containsAll(allUsers), is(true));
    }

}
