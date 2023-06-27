package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    private User makeUser(Long id, String name, String email) {
        return User.builder().id(id).name(name).email(email).build();
    }

    private Item makeItem(Long id, String name, String desc, User owner, Boolean available) {
        return Item.builder().id(id).name(name).description(desc).owner(owner).available(available).build();
    }

    private Request makeRequest(String desc) {
        return Request.builder()
                .description(desc)
                .build();
    }

    @Test
    void addNotFoundRequesterTest() {
        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        User requester = makeUser(2L, "requester", "requester@ya.ru");
        Item item = makeItem(3L, "item", "itemDesc", itemOwner, true);
        Request request = makeRequest("desc request");

        RequestService requestService = new RequestServiceImpl(userRepository, itemRepository, requestRepository);
        when(userRepository.findById(anyLong())).thenThrow(new EntityNotFoundException("Пользователь не найден"));

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> requestService.add(request, requester.getId()));

        assertThat(exception.getMessage(), equalTo("Пользователь не найден"));
        verify(requestRepository, times(0)).save(any());
    }

    @Test
    void addTest() {
        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        User requester = makeUser(2L, "requester", "requester@ya.ru");
        Request request = makeRequest("desc req");

        RequestService requestService = new RequestServiceImpl(userRepository, itemRepository, requestRepository);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester));
        when(requestRepository.save(any())).thenReturn(request);

        Request createdRequest = requestService.add(request, requester.getId());

        verify(requestRepository, times(1)).save(any());
        assertThat(createdRequest.getCreated(), notNullValue());
        assertThat(createdRequest.getDescription(), equalTo("desc req"));
        assertThat(createdRequest.getItems().size(), equalTo(0));
    }

    @Test
    void getAllByUserNotFoundUser() {
        User requester = makeUser(2L, "requester", "requester@ya.ru");

        RequestService requestService = new RequestServiceImpl(userRepository, itemRepository, requestRepository);
        when(userRepository.findById(anyLong())).thenThrow(new EntityNotFoundException("Пользователь не найден"));

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> requestService.getAllByUser(requester.getId()));

        assertThat(exception.getMessage(), equalTo("Пользователь не найден"));
        verify(requestRepository, times(0)).save(any());
    }

    @Test
    void getAllByUser() {
        RequestService requestService = new RequestServiceImpl(userRepository, itemRepository, requestRepository);

        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        User requester1 = makeUser(2L, "requester", "requester@ya.ru");
        Item item1 = makeItem(4L, "item", "itemDesc", itemOwner, true);
        Item item2 = makeItem(5L, "item2", "itemDesc2", itemOwner, true);
        Request request1 = makeRequest("desc request 1");
        request1.setId(6L);
        item1.setRequest(request1);
        Request request2 = makeRequest("desc request 2");
        request2.setId(7L);
        item2.setRequest(request2);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester1));
        when(requestRepository.findAllByRequesterId(anyLong())).thenReturn(List.of(request1, request2));
        when(itemRepository.findByRequestIn(any())).thenReturn(List.of(item1, item2));

        List<Request> requestsByUser = requestService.getAllByUser(request1.getId());

        assertThat(requestsByUser.size(), equalTo(2));
        assertThat(requestsByUser.get(0).getItems().get(0).getId(), equalTo(item1.getId()));
        assertThat(requestsByUser.get(1).getItems().get(0).getId(), equalTo(item2.getId()));
    }

    @Test
    void getAllByUserEmpty() {
        RequestService requestService = new RequestServiceImpl(userRepository, itemRepository, requestRepository);

        User requester1 = makeUser(2L, "requester", "requester@ya.ru");
        Request request1 = makeRequest("desc request 1");
        request1.setId(6L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester1));
        when(requestRepository.findAllByRequesterId(anyLong())).thenReturn(List.of());

        List<Request> requestsByUser = requestService.getAllByUser(request1.getId());

        assertThat(requestsByUser.size(), equalTo(0));
        verify(itemRepository, times(0)).findByRequestIn(any());
    }

    @Test
    void getByIdNotFoundUser() {
        User itemOwner = makeUser(1L, "owner", "owner@ya.ru");
        Request request1 = makeRequest("desc request 1");
        request1.setId(6L);

        RequestService requestService = new RequestServiceImpl(userRepository, itemRepository, requestRepository);
        when(userRepository.findById(anyLong())).thenThrow(new EntityNotFoundException("Пользователь не найден"));

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> requestService.getById(request1.getId(), itemOwner.getId()));

        assertThat(exception.getMessage(), equalTo("Пользователь не найден"));
        verify(requestRepository, times(0)).save(any());
    }

    @Test
    void getByIdNotFoundRequest() {
        RequestService requestService = new RequestServiceImpl(userRepository, itemRepository, requestRepository);

        User itemOwner = makeUser(1L, "owner", "owner@ya.ru");
        User requester1 = makeUser(2L, "requester", "requester@ya.ru");
        Request request1 = makeRequest("desc request 1");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester1));
        when(requestRepository.findById(anyLong())).thenThrow(new EntityNotFoundException("Запрос не найден"));

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> requestService.getById(request1.getId(), itemOwner.getId()));

        assertThat(exception.getMessage(), equalTo("Запрос не найден"));
        verify(itemRepository, times(0)).findByRequest(any());
    }

    @Test
    void getByIdTest() {
        RequestService requestService = new RequestServiceImpl(userRepository, itemRepository, requestRepository);

        User itemOwner = makeUser(1L, "user", "user@ya.ru");
        User requester1 = makeUser(2L, "requester", "requester@ya.ru");
        Item item1 = makeItem(4L, "item", "itemDesc", itemOwner, true);
        Request request1 = makeRequest("desc request 1");
        request1.setId(6L);
        item1.setRequest(request1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requester1));
        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request1));
        when(itemRepository.findByRequest(request1)).thenReturn(List.of(item1));

        Request byId = requestService.getById(request1.getId(), itemOwner.getId());

        verify(itemRepository, times(1)).findByRequest(any());
        assertThat(byId.getItems().size(), equalTo(1));
    }

    @Test
    void getAllNullParamsTest() {
        RequestService requestService = new RequestServiceImpl(userRepository, itemRepository, requestRepository);

        List<Request> all = requestService.getAll(1L, null, null);

        assertThat(all.size(), equalTo(0));
        verify(userRepository, times(0)).findById(any());
    }

    @Test
    void getAllUnavailableParamsTest() {
        RequestService requestService = new RequestServiceImpl(userRepository, itemRepository, requestRepository);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> requestService.getAll(1L, 0, 0));

        assertTrue(exception.getMessage().contains("Некорректные значения параметров from, size"));
    }

    @Test
    void getAllNotFoundTest() {
        RequestService requestService = new RequestServiceImpl(userRepository, itemRepository, requestRepository);

        when(userRepository.findById(anyLong())).thenThrow(new EntityNotFoundException("Пользователь не найден"));

        final EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> requestService.getAll(1L, 0, 10));

        assertTrue(exception.getMessage().contains("Пользователь не найден"));
    }
}