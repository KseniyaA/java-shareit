package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.EntityNotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;

    private final RequestRepository requestRepository;

    @Transactional
    @Override
    public Request add(Request request, long requesterId) {
        User requester = userRepository.findById(requesterId).orElseThrow(() -> {
            throw new EntityNotFoundException("Пользователь с id = " + requesterId + " не существует");
        });
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());
        request.setItems(Collections.emptyList());
        return requestRepository.save(request);
    }

    @Override
    public List<Request> getAllByUser(long userId) {
        userRepository.findById(userId).orElseThrow(() -> {
            throw new EntityNotFoundException("Пользователь с id = " + userId + " не существует");
        });
        List<Request> allByRequesterId = requestRepository.findAllByRequesterId(userId);
        if (allByRequesterId.isEmpty()) {
            return allByRequesterId;
        }
        Map<Request, List<Item>> itemsByRequest = itemRepository.findByRequestIn(allByRequesterId)
                .stream()
                .collect(groupingBy(Item::getRequest, toList()));
        allByRequesterId.forEach(x -> x.setItems(itemsByRequest.get(x)));
        return allByRequesterId;
    }

    @Override
    public List<Request> getAll(long userId, Integer from, Integer size) {
        if (from == null && size == null) {
            return Collections.emptyList();
        }
        if (!(from >= 0 && size > 0)) {
            throw new ValidationException("Некорректные значения параметров from, size");
        }
        userRepository.findById(userId).orElseThrow(() -> {
            throw new EntityNotFoundException("Пользователь с id = " + userId + " не существует");
        });

        List<Request> requestsResult = new ArrayList<>();
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);
        do {
            Page<Request> requestsPage = requestRepository.findAll(userId, page);
            List<Request> requests = requestsPage.getContent();
            if (requests.isEmpty()) {
                return requestsResult;
            }
            Map<Request, List<Item>> itemsByRequest = itemRepository.findByRequestIn(requests)
                    .stream()
                    .collect(groupingBy(Item::getRequest, toList()));
            requests.forEach(x -> x.setItems(itemsByRequest.get(x)));
            requestsResult.addAll(requests);
            if (requestsPage.hasNext()) {
                page = PageRequest.of(requestsPage.getNumber() + 1, requestsPage.getSize(), requestsPage.getSort());
            } else {
                page = null;
            }
        } while (page != null);
        return requestsResult;
    }

    @Override
    public Request getById(long id, long userId) {
        userRepository.findById(userId).orElseThrow(() -> {
            throw new EntityNotFoundException("Пользователь с id = " + userId + " не существует");
        });
        Request request = requestRepository.findById(id).orElseThrow(() -> {
            throw new EntityNotFoundException("Запрос с id = " + id + " не найден");
        });
        List<Item> itemsByRequest = itemRepository.findByRequest(request);
        request.setItems(itemsByRequest);
        return request;
    }
}
