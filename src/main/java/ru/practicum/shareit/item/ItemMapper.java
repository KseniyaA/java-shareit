package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserMapper;

import java.util.ArrayList;
import java.util.List;

public class ItemMapper {

    public static Item toItemCreateRequest(ItemDtoCreateRequest itemDtoCreateRequest) {
        return Item.builder()
                .name(itemDtoCreateRequest.getName())
                .description(itemDtoCreateRequest.getDescription())
                .available(itemDtoCreateRequest.getAvailable())
                .build();
    }

    public static ItemDtoResponse toItemDtoResponse(Item item) {
        ItemRequestDto itemRequestDto = null;
        if (item.getRequest() != null) {
            itemRequestDto = ItemRequestMapper.toItemRequestDto(item.getRequest());
        }
        return ItemDtoResponse.builder()
                .name(item.getName())
                .description(item.getDescription())
                .owner(UserMapper.toUserDto(item.getOwner()))
                .id(item.getId())
                .request(itemRequestDto)
                .available(item.getAvailable())
                .build();
    }

    public static Item toItemFromItemDtoUpdateRequest(ItemDtoUpdateRequest itemDtoUpdateRequest, long itemId) {
        return Item.builder()
                .id(itemId)
                .name(itemDtoUpdateRequest.getName())
                .description(itemDtoUpdateRequest.getDescription())
                .available(itemDtoUpdateRequest.getAvailable())
                .build();
    }


    /*private static ItemDtoShortResponse toItemDtoShortResponse(Item item) {
        return ItemDtoShortResponse.builder()
                .name(item.getName())
                .description(item.getDescription())
                .build();
    }

    public static List<ItemDtoShortResponse> toItemDtoShortResponseList(List<Item> allByUser) {
        List<ItemDtoShortResponse> all = new ArrayList<>();
        for (Item item : allByUser) {
            all.add(toItemDtoShortResponse(item));
        }
        return all;
    }*/

    public static List<ItemDtoResponse> toItemDtoResponseList(List<Item> items) {
        List<ItemDtoResponse> itemDtoResponseList = new ArrayList<>();
        for(Item item : items) {
            itemDtoResponseList.add(toItemDtoResponse(item));
        }
        return itemDtoResponseList;
    }
}
