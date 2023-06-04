package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
@Builder
public class Item {
    private long id;
    private String name;
    private String description;
    private User owner;
    private Boolean available; // Статус должен проставлять владелец.
    private ItemRequest request;
}
