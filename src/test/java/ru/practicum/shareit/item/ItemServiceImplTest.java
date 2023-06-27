package ru.practicum.shareit.item;

import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.exceptions.ItemIncorrectOwnerException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

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

    private Comment makeComment(Long id, String text, Item item, User author) {
        return Comment.builder()
                .id(id)
                .text(text)
                .item(item)
                .author(author)
                .build();
    }

    @Test
    void addWithSuccessResponseTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        Item itemIn = makeItem("name", "desc", false);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(itemRepository.save(itemIn)).thenReturn(itemIn);

        Item createdItem = itemService.add(itemIn, 1L);

        assertThat(createdItem.getName(), equalTo("name"));
        assertThat(createdItem.getDescription(), equalTo("desc"));
        assertFalse(createdItem.getAvailable());
        assertThat(createdItem.getOwner().getId(), equalTo(1L));
        assertThat(createdItem.getRequest(), nullValue());
        assertThat(createdItem.getComments(), nullValue());

        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void addNotFoundOwnerIdTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        Item itemIn = makeItem("name", "desc", false);

        when(userRepository.findById(anyLong()))
                .thenThrow(new EntityNotFoundException("Пользователь не найден"));

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> itemService.add(itemIn, 1L));

        Assertions.assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void updateSuccessTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        User user = makeUser(1L, "name", "email");
        Item itemReq = makeItem(1L, "name", "desc", true, user);
        Item itemRes = makeItem(1L, "updatedName", "updatedDesc", false, user);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(itemReq));
        when(commentRepository.findByItem(any(), any())).thenReturn(null);
        when(bookingRepository.findByItemAndStatusOrderByStartAsc(any(), any())).thenReturn(Collections.emptyList());

        Item updatedItem = itemService.update(itemRes, user.getId());

        assertThat(updatedItem.getName(), equalTo(itemRes.getName()));
    }

    @Test
    void updateUserNotFoundTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        User user = makeUser(1L, "name", "email");
        Item itemRes = makeItem(1L, "updatedName", "updatedDesc", false, user);

        when(userRepository.findById(anyLong()))
                .thenThrow(new EntityNotFoundException("Пользователь не найден"));;

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> itemService.update(itemRes, user.getId()));

        Assertions.assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void updateOwnerNotEqUserTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        User user1 = makeUser(1L, "name", "email");
        Item itemReq = makeItem(1L, "updatedName", "updatedDesc", false, user1);
        User user2 = makeUser(2L, "name2", "email2");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(itemReq));
        when(commentRepository.findByItem(any(), any())).thenReturn(null);
        when(bookingRepository.findByItemAndStatusOrderByStartAsc(any(), any())).thenReturn(Collections.emptyList());

        final ItemIncorrectOwnerException exception = Assertions.assertThrows(
                ItemIncorrectOwnerException.class,
                () -> itemService.update(itemReq, user2.getId()));

        assertThat("Пользователь с id = 2 не является владельцем вещи с id = 1", equalTo(exception.getMessage()));
    }

    @Test
    void getTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        User user1 = makeUser(1L, "name", "email");
        Item itemReq = makeItem(1L, "updatedName", "updatedDesc", false, user1);
        User user2 = makeUser(2L, "name2", "email2");

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(itemReq));
        when(commentRepository.findByItem(any(), any())).thenReturn(null);
        when(bookingRepository.findByItemAndStatusOrderByStartAsc(any(), any())).thenReturn(Collections.emptyList());

        Item selectedItem = itemService.get(itemReq.getId(), user2.getId());

        assertThat(selectedItem.getLastBooking(), nullValue());
        assertThat(selectedItem.getNextBooking(), nullValue());
    }

    @Test
    void getAllByUserValidationFailTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.getAllByUser(1L, 0, 0));
        assertTrue(exception.getMessage().contains("Некорректные значения для параметров from, size"));

        final ValidationException exception2 = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.getAllByUser(1L, -1, 1));
        assertTrue(exception2.getMessage().contains("Некорректные значения для параметров from, size"));

        final ValidationException exception3 = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.getAllByUser(1L, 0, -1));
        assertTrue(exception3.getMessage().contains("Некорректные значения для параметров from, size"));
    }

    @Test
    void searchByNullBlankTextTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        List<Item> items = itemService.searchByText("", 0, 10);
        assertThat(items.size(), equalTo(0));

        List<Item> items2 = itemService.searchByText("", 0, 10);
        assertThat(items2.size(), equalTo(0));
    }

    @Test
    void searchByTextTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

        final ValidationException exception1 = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.searchByText("text", 0, 0));
        assertTrue(exception1.getMessage().contains("Некорректные значения для параметров from, size"));

        final ValidationException exception2 = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.searchByText("text", -1, 0));
        assertTrue(exception2.getMessage().contains("Некорректные значения для параметров from, size"));

        final ValidationException exception3 = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.searchByText("text", 0, -1));
        assertTrue(exception3.getMessage().contains("Некорректные значения для параметров from, size"));
    }

    @Test
    void searchByTextPageSuccessTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);
        User user1 = makeUser(1L, "name", "email");
        Item itemReq = makeItem(1L, "updatedName", "updatedDesc", false, user1);

        when(itemRepository.searchByText(anyString(), any())).thenReturn(mock(Page.class));

        itemService.searchByText("text", 0, 10);

        verify(itemRepository, times(1)).searchByText(anyString(), any());
        verify(itemRepository, times(0)).searchByText(anyString());
    }

    @Test
    void searchByTextSuccessTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);
        User user1 = makeUser(1L, "name", "email");
        Item itemReq = makeItem(1L, "updatedName", "updatedDesc", false, user1);

        when(itemRepository.searchByText(anyString())).thenReturn(List.of(itemReq));

        itemService.searchByText("text", null, null);

        verify(itemRepository, times(0)).searchByText(anyString(), any());
        verify(itemRepository, times(1)).searchByText(anyString());
    }

    @Test
    void getAllByUserPageSuccessTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);
        User user1 = makeUser(1L, "name", "email");
        Item itemReq = makeItem(1L, "updatedName", "updatedDesc", false, user1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findAllByOwnerId(anyLong(), any())).thenReturn(mock(Page.class));

        itemService.getAllByUser(1L, 0, 10);

        verify(itemRepository, times(1)).findAllByOwnerId(anyLong(), any());
        verify(itemRepository, times(0)).findAllByOwnerId(anyLong());
    }

    @Test
    void getAllByUserNoPageSuccessTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);
        User user1 = makeUser(1L, "name", "email");
        Item itemReq = makeItem(1L, "updatedName", "updatedDesc", false, user1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(List.of(itemReq));

        itemService.getAllByUser(1L, null, null);

        verify(itemRepository, times(0)).findAllByOwnerId(anyLong(), any());
        verify(itemRepository, times(1)).findAllByOwnerId(anyLong());
    }

    @Test
    void createCommentSuccessTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);
        User user = makeUser(1L, "name", "email");
        Item item = makeItem(1L, "name", "desc", true, user);
        Comment comment = makeComment(1L, "text", item, user);

        when(itemRepository.findById(any())).thenReturn(Optional.of(item));
        when(bookingRepository.exists((Predicate) any())).thenReturn(true);
        when(userRepository.getReferenceById(anyLong())).thenReturn(user);

        itemService.createComment(comment, user.getId(), item.getId());

        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void createCommentValidationTest() {
        ItemService itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);
        User user = makeUser(1L, "name", "email");
        Item item = makeItem(1L, "name", "desc", true, user);
        Comment comment = makeComment(1L, "text", item, user);

        when(bookingRepository.exists((Predicate) any()))
                .thenReturn(false);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> itemService.createComment(comment, 1L, 1L));
        assertTrue(exception.getMessage().contains("Пользователь с id = 1 не брал в аренду вещь с id = 1"));

        verify(commentRepository, times(0)).save(any());
    }

}