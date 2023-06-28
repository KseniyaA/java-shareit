package ru.practicum.shareit.booking;

import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.exception.BookingIncorrectDataException;
import ru.practicum.shareit.booking.exception.BookingUnavailableOperationException;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    private User makeUser(Long id, String name, String email) {
        return User.builder().id(id).name(name).email(email).build();
    }

    private Item makeItem(Long id, String name, String desc, User owner, Boolean available) {
        return Item.builder().id(id).name(name).description(desc).owner(owner).available(available).build();
    }

    private Booking makeBooking(Item item) {
        return Booking.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .build();
    }

    @Test
    void createItemNotFound() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);
        User booker = makeUser(1L, "name", "email");
        User itemOwner = makeUser(2L, "name2", "email2");
        Item item = makeItem(3L, "name", "desc", itemOwner, true);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> service.create(makeBooking(item), booker));
        assertThat(exception.getMessage(), equalTo("Вещь с id = " + item.getId() + " не найдена"));

        verify(itemRepository, times(0)).save(any());
    }

    @Test
    void createItemOwnerItemFailTest() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        Item item = makeItem(1L, "item", "desc", itemOwner, true);
        Booking bookingDto = makeBooking(item);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> service.create(bookingDto, itemOwner));
        assertThat(exception.getMessage(), equalTo("Владелец вещи не может забронировать вещь"));

        verify(itemRepository, times(0)).save(any());
    }

    @Test
    void createUnavailableItemBookingFailTest() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        Item item = makeItem(2L, "item", "desc", itemOwner, false);
        User booker = makeUser(3L, "user2", "user2@ya.ru");
        Booking bookingDto = makeBooking(item);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        final BookingIncorrectDataException exception = Assertions.assertThrows(
                BookingIncorrectDataException.class,
                () -> service.create(bookingDto, booker));
        assertThat(exception.getMessage(), equalTo("Вещь с id = 2 не доступна для бронирования"));

        verify(itemRepository, times(0)).save(any());
    }

    @Test
    void createTest() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        Item item = makeItem(2L, "item", "desc", itemOwner, true);
        User booker = makeUser(3L, "user2", "user2@ya.ru");
        Booking bookingDto = makeBooking(item);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(bookingDto)).thenReturn(bookingDto);

        Booking createdBooking = service.create(bookingDto, booker);

        assertThat(createdBooking.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(createdBooking.getBooker().getId(), equalTo(booker.getId()));

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void update() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        Item item = makeItem(2L, "item", "desc", itemOwner, true);
        Booking bookingDto = makeBooking(item);
        when(bookingRepository.save(bookingDto)).thenReturn(bookingDto);

        service.update(bookingDto);

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void deleteById() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        doNothing().when(bookingRepository).deleteById(anyLong());

        service.deleteById(1L);
        verify(bookingRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void getByIdNotFoundTest() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> service.getById(1L));

        Assertions.assertEquals("Бронирование c id = 1 не найдено", exception.getMessage());
    }

    @Test
    void getByIdTest() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        Item item = makeItem(2L, "item", "desc", itemOwner, true);
        Booking bookingDto = makeBooking(item);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingDto));

        Booking byId = service.getById(1L);

        verify(bookingRepository, times(1)).findById(anyLong());
        assertThat(byId, notNullValue());
    }

    @Test
    void approveNotOwner() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        Item item = makeItem(2L, "item", "desc", itemOwner, true);
        Booking bookingDto = makeBooking(item);
        bookingDto.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingDto));

        final BookingUnavailableOperationException exception = Assertions.assertThrows(
                BookingUnavailableOperationException.class,
                () -> service.approve(bookingDto.getId(), false, 999L));

        assertThat("Подтверждение или отклонение запроса может быть выполнено " +
                "только владельцем вещи", equalTo(exception.getMessage()));
    }

    @Test
    void approveSameStatus() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        Item item = makeItem(2L, "item", "desc", itemOwner, true);
        Booking bookingDto = makeBooking(item);
        bookingDto.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingDto));

        final BookingIncorrectDataException exception = Assertions.assertThrows(
                BookingIncorrectDataException.class,
                () -> service.approve(bookingDto.getId(), true, 1L));

        assertThat("Статус уже изменен", equalTo(exception.getMessage()));
    }

    @Test
    void approveSuccessTest() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        Item item = makeItem(2L, "item", "desc", itemOwner, true);
        Booking bookingDto = makeBooking(item);
        bookingDto.setStatus(BookingStatus.REJECTED);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingDto));

        Booking approvedBooking = service.approve(bookingDto.getId(), true, 1L);

        assertThat(approvedBooking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void get() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        Item item = makeItem(2L, "item", "desc", itemOwner, true);
        User booker = makeUser(3L, "user2", "user2@ya.ru");
        Booking bookingDto = makeBooking(item);
        bookingDto.setBooker(booker);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingDto));

        final BookingUnavailableOperationException exception = Assertions.assertThrows(
                BookingUnavailableOperationException.class,
                () -> service.get(bookingDto.getId(), 10L));

        assertThat("Получение данных о бронировании может быть выполнено " +
                "либо автором бронирования, либо владельцем вещи", equalTo(exception.getMessage()));
    }

    @Test
    void getAllBookingsByUserIncorrectFromSize() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        BookingIncorrectDataException exception = Assertions.assertThrows(
                BookingIncorrectDataException.class,
                () -> service.getAllBookingsByUser(1L, "ALL", 0, 0));

        assertThat("Некорректные значения для параметров from, size", equalTo(exception.getMessage()));

        exception = Assertions.assertThrows(
                BookingIncorrectDataException.class,
                () -> service.getAllBookingsByUser(1L, "ALL", -1, 0));

        assertThat("Некорректные значения для параметров from, size", equalTo(exception.getMessage()));

        exception = Assertions.assertThrows(
                BookingIncorrectDataException.class,
                () -> service.getAllBookingsByUser(1L, "ALL", 0, -1));

        assertThat("Некорректные значения для параметров from, size", equalTo(exception.getMessage()));
    }

    @Test
    void getAllBookingsByUserPageableTest() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);
        Page<Booking> bookingsPage = Page.empty();

        when(bookingRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(bookingsPage);

        service.getAllBookingsByUser(1L, "ALL", 0, 10);

        verify(bookingRepository, times(1)).findAll(any(Predicate.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsByUserTest() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);
        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        Item item = makeItem(2L, "item", "desc", itemOwner, true);
        User booker = makeUser(3L, "user2", "user2@ya.ru");
        Booking bookingDto = makeBooking(item);
        bookingDto.setBooker(booker);

        when(bookingRepository.findAll(any(Predicate.class), any(Sort.class))).thenReturn(List.of(bookingDto));

        service.getAllBookingsByUser(1L, "ALL", null, null);

        verify(bookingRepository, times(1)).findAll(any(Predicate.class), any(Sort.class));
        verify(bookingRepository, times(0)).findAll(any(Predicate.class), any(Pageable.class));
    }

    @Test
    void getAllBookingsByItemOwnerIncorrectFromSize() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);

        BookingIncorrectDataException exception = Assertions.assertThrows(
                BookingIncorrectDataException.class,
                () -> service.getAllBookingsByItemOwner(1L, "ALL", 0, 0));

        assertThat("Некорректные значения для параметров from, size", equalTo(exception.getMessage()));

        exception = Assertions.assertThrows(
                BookingIncorrectDataException.class,
                () -> service.getAllBookingsByItemOwner(1L, "ALL", -1, 0));

        assertThat("Некорректные значения для параметров from, size", equalTo(exception.getMessage()));

        exception = Assertions.assertThrows(
                BookingIncorrectDataException.class,
                () -> service.getAllBookingsByItemOwner(1L, "ALL", 0, -1));

        assertThat("Некорректные значения для параметров from, size", equalTo(exception.getMessage()));

    }

    @Test
    void getAllBookingsByItemOwnerPageableTest() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);
        Page<Booking> bookingsPage = Page.empty();

        when(bookingRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(bookingsPage);

        service.getAllBookingsByItemOwner(1L, "ALL", 0, 10);

        verify(bookingRepository, times(1)).findAll(any(Predicate.class), any(Pageable.class));

    }

    @Test
    void getAllBookingsByItemOwnerTest() {
        BookingService service = new BookingServiceImpl(bookingRepository, itemRepository);
        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        Item item = makeItem(2L, "item", "desc", itemOwner, true);
        User booker = makeUser(3L, "user2", "user2@ya.ru");
        Booking bookingDto = makeBooking(item);
        bookingDto.setBooker(booker);

        when(bookingRepository.findAll(any(Predicate.class), any(Sort.class))).thenReturn(List.of(bookingDto));

        service.getAllBookingsByItemOwner(1L, "ALL", null, null);

        verify(bookingRepository, times(1)).findAll(any(Predicate.class), any(Sort.class));
        verify(bookingRepository, times(0)).findAll(any(Predicate.class), any(Pageable.class));
    }
}