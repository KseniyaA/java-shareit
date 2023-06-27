package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceDbTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;

    private Item makeItem(String name, String desc, boolean isAvailable) {
        return Item.builder()
                .name(name)
                .description(desc)
                .available(isAvailable)
                .build();
    }

    private Item makeItem(Long id, String name, String desc, boolean isAvailable, User owner) {
        return Item.builder()
                .id(id)
                .name(name)
                .description(desc)
                .available(isAvailable)
                .owner(owner)
                .build();
    }

    private User makeUser(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }

    private User makeUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }

    private Comment makeComment(Long id, String text, Item item, User author) {
        return Comment.builder()
                .id(id)
                .text(text)
                .item(item)
                .author(author)
                .build();
    }

    @BeforeEach
    private void clear() {
        em.createQuery("delete from Item").executeUpdate();
        em.createQuery("delete from User").executeUpdate();
    }

    @Test
    void addItemSuccessTest() {
        User createdUser = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item itemIn = makeItem("name", "desc", false);

        itemService.add(itemIn, createdUser.getId());

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item user = query
                .setParameter("name", itemIn.getName())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(itemIn.getName()));
        assertThat(user.getDescription(), equalTo(itemIn.getDescription()));
        assertFalse(user.getAvailable());
        assertThat(user.getOwner().getId(), equalTo(1L));
    }

    @Test
    void addItemUserNotFoundTest() {
        Item itemIn = makeItem("name", "desc", false);

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> itemService.add(itemIn, 999L));

        assertThat("Пользователь с id = 999 не существует", equalTo(exception.getMessage()));
    }

    @Test
    void updateItemSuccessTest() {
        User createdUser = userService.create(makeUser("userName", "userEmail@ya.ru"));
        Item itemIn = makeItem("name", "desc", false);
        Item createdItemInDb = itemService.add(itemIn, createdUser.getId());
        Item updatedItemDto = createdItemInDb;
        updatedItemDto.setName("updatedName");
        updatedItemDto.setDescription("updatedDesc");
        updatedItemDto.setAvailable(true);

        itemService.update(updatedItemDto, createdUser.getId());

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.id= :id", Item.class);
        Item selectedItem = query
                .setParameter("id", createdItemInDb.getId())
                .getSingleResult();

        assertThat(selectedItem.getId(), equalTo(updatedItemDto.getId()));
        assertThat(selectedItem.getName(), equalTo(updatedItemDto.getName()));
        assertThat(selectedItem.getDescription(), equalTo(updatedItemDto.getDescription()));
        assertTrue(selectedItem.getAvailable());
        assertThat(selectedItem.getOwner().getId(), equalTo(createdUser.getId()));
    }

    @Test
    void getItemSuccessTest() {
        User userDto = makeUser("name", "name@ya.ru");
        User createdUserInDbn = userService.create(userDto);
        Item itemDto = makeItem("name", "desc", false);
        Item createdItemInDb = itemService.add(itemDto, createdUserInDbn.getId());

        Item item = itemService.get(createdItemInDb.getId(), createdItemInDb.getId());

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.id = :id", Item.class);
        Item selectedItem = query
                .setParameter("id", createdItemInDb.getId())
                .getSingleResult();

        assertThat(selectedItem.getName(), equalTo(item.getName()));
        assertThat(selectedItem.getDescription(), equalTo(item.getDescription()));
        assertThat(selectedItem.getAvailable(), equalTo(item.getAvailable()));
    }

    @Test
    void getAllByUserSuccessTest() {
        User createdUserInDb1 = userService.create(makeUser("name", "name@ya.ru"));
        User createdUserInDb2 = userService.create(makeUser("name2", "name2@ya.ru"));
        itemService.add(makeItem("name1", "desc1", false), createdUserInDb1.getId());
        itemService.add(makeItem("name2", "desc2", false), createdUserInDb1.getId());
        itemService.add(makeItem("name3", "desc3", false), createdUserInDb2.getId());

        List<Item> itemByUser1 = itemService.getAllByUser(createdUserInDb1.getId(), null, null);
        List<Item> itemByUser2 = itemService.getAllByUser(createdUserInDb2.getId(), null, null);

        TypedQuery<Item> query = em.createQuery("select i from Item i join i.owner as u where u.id = :id", Item.class);
        List<Item> selectedItemByUser1 = query
                .setParameter("id", createdUserInDb1.getId())
                .getResultList();
        List<Item> selectedItemByUser2 = query
                .setParameter("id", createdUserInDb2.getId())
                .getResultList();

        assertThat(selectedItemByUser1.size(), equalTo(itemByUser1.size()));
        assertThat(selectedItemByUser2.size(), equalTo(itemByUser2.size()));
    }

    @Test
    void searchByTextSuccessTest() {
        User createdUserInDb1 = userService.create(makeUser("name", "name@ya.ru"));
        User createdUserInDb2 = userService.create(makeUser("name2", "name2@ya.ru"));
        Item createdItem1 = itemService.add(makeItem("name1", "desc1", false), createdUserInDb1.getId());
        itemService.add(makeItem("name2", "desc2", false), createdUserInDb1.getId());
        itemService.add(makeItem("name3", "desc3", false), createdUserInDb2.getId());

        List<Item> filteredItems = itemService.searchByText("NAME", null, null);

        assertThat(filteredItems.size(), equalTo(0));

        createdItem1.setAvailable(true);
        itemService.update(createdItem1, createdUserInDb1.getId());

        filteredItems = itemService.searchByText("NAME", null, null);

        assertThat(filteredItems.size(), equalTo(1));

    }

    private Booking makeBooking(User booker, Item item) {
        return Booking.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .booker(booker)
                .item(item)
                .build();
    }

    @Test
    void nextAndLastBookingSuccessTest() {
        User itemOwner = userService.create(makeUser("name", "name@ya.ru"));
        Item createdItem1 = itemService.add(makeItem("name1", "desc1", true), itemOwner.getId());
        Item item = itemService.add(makeItem("name2", "desc2", true), itemOwner.getId());
        User booker = userService.create(makeUser("booker", "booker@ya.ru"));

        Booking bookingDto1 = makeBooking(booker, item);
        bookingDto1.setStart(LocalDateTime.now().minusMinutes(100));
        bookingDto1.setEnd(LocalDateTime.now().minusMinutes(50));
        Booking booking1 = bookingService.create(bookingDto1, booker);
        bookingService.approve(booking1.getId(), true, itemOwner.getId());

        Booking bookingDto2 = makeBooking(booker, item);
        bookingDto2.setStart(LocalDateTime.now().minusMinutes(10));
        bookingDto2.setEnd(LocalDateTime.now().plusMinutes(10));
        Booking booking2 = bookingService.create(bookingDto2, booker);
        bookingService.approve(booking2.getId(), true, itemOwner.getId());

        Booking bookingDto3 = makeBooking(booker, item);
        bookingDto3.setStart(LocalDateTime.now().plusMinutes(100));
        bookingDto3.setEnd(LocalDateTime.now().plusMinutes(150));
        Booking booking3 = bookingService.create(bookingDto3, booker);
        bookingService.approve(booking3.getId(), true, itemOwner.getId());

        Item selectedItem = itemService.get(item.getId(), itemOwner.getId());

        assertThat(selectedItem.getLastBooking().getId(), equalTo(booking2.getId()));
        assertThat(selectedItem.getNextBooking().getId(), equalTo(booking3.getId()));

        // check when booking not approved
        bookingService.approve(booking3.getId(), false, itemOwner.getId());
        selectedItem = itemService.get(item.getId(), itemOwner.getId());
        assertThat(selectedItem.getLastBooking().getId(), equalTo(booking2.getId()));
        assertThat(selectedItem.getNextBooking(), nullValue());

        // check next and last booking with created another booking
        Booking bookingDto4 = makeBooking(booker, item);
        bookingDto4.setStart(LocalDateTime.now().plusMinutes(50));
        bookingDto4.setEnd(LocalDateTime.now().plusMinutes(70));
        Booking booking4 = bookingService.create(bookingDto4, booker);
        bookingService.approve(booking4.getId(), true, itemOwner.getId());

        selectedItem = itemService.get(item.getId(), itemOwner.getId());
        assertThat(selectedItem.getLastBooking().getId(), equalTo(booking2.getId()));
        assertThat(selectedItem.getNextBooking().getId(), equalTo(booking4.getId()));
    }

    @Test
    void checkCommentsSuccessTest() {
        User itemOwner = userService.create(makeUser("name", "name@ya.ru"));
        Item createdItem1 = itemService.add(makeItem("name1", "desc1", true), itemOwner.getId());
        Item item = itemService.add(makeItem("name2", "desc2", true), itemOwner.getId());
        User booker = userService.create(makeUser("booker", "booker@ya.ru"));

        Booking bookingDto1 = makeBooking(booker, item);
        bookingDto1.setStart(LocalDateTime.now().minusMinutes(100));
        bookingDto1.setEnd(LocalDateTime.now().minusMinutes(50));
        Booking booking1 = bookingService.create(bookingDto1, booker);
        bookingService.approve(booking1.getId(), true, itemOwner.getId());

        Booking bookingDto2 = makeBooking(booker, item);
        bookingDto2.setStart(LocalDateTime.now().minusMinutes(10));
        bookingDto2.setEnd(LocalDateTime.now().plusMinutes(10));
        Booking booking2 = bookingService.create(bookingDto2, booker);
        bookingService.approve(booking2.getId(), true, itemOwner.getId());

        Comment createdComment = itemService.createComment(Comment.builder().text("text comment").build(),
                booker.getId(), item.getId());

        TypedQuery<Comment> query = em.createQuery("select c from Comment c join c.item as i where i.id = :itemId",
                Comment.class);
        List<Comment> commentDb = query
                .setParameter("itemId", item.getId())
                .getResultList();

        Item selectedItem = itemService.get(item.getId(), itemOwner.getId());
        assertThat(selectedItem.getComments().size(), equalTo(1));
        assertThat(commentDb.size(), equalTo(1));
        assertThat(selectedItem.getComments().get(0).getText(), equalTo("text comment"));
        assertThat(commentDb.get(0).getText(), equalTo("text comment"));
    }
}
