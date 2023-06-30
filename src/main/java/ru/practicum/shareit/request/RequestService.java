package ru.practicum.shareit.request;

import java.util.List;

public interface RequestService {
    Request add(Request request, long requesterId);

    List<Request>  getAllByUser(long userId);

    List<Request> getAll(long userId, Integer from, Integer size);

    Request getById(long id, long userId);
}
