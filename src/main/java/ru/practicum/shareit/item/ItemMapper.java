package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingDateResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.RequestMapper;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.UserMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemMapper {

    public Item toItemRequest(ItemDtoRequest itemDtoCreateRequest) {
        return Item.builder()
                .name(itemDtoCreateRequest.getName())
                .description(itemDtoCreateRequest.getDescription())
                .available(itemDtoCreateRequest.getAvailable())
                .build();
    }

    public Item toItemRequest(ItemDtoRequest itemDtoRequest, Long itemId) {
        return Item.builder()
                .id(itemId)
                .name(itemDtoRequest.getName())
                .description(itemDtoRequest.getDescription())
                .available(itemDtoRequest.getAvailable())
                .build();
    }

    public ItemDtoResponse toItemDtoResponse(Item item) {
        RequestDto requestDto = null;
        if (item.getRequest() != null) {
            requestDto = RequestMapper.toRequestDto(item.getRequest());
        }
        return ItemDtoResponse.builder()
                .name(item.getName())
                .description(item.getDescription())
                .owner(UserMapper.toUserDtoRequest(item.getOwner()))
                .id(item.getId())
                .request(requestDto)
                .available(item.getAvailable())
                .build();
    }

    public ItemDtoWithBookingDateResponse toItemDtoWithBookingDateResponse(Item item) {
        RequestDto requestDto = null;
        if (item.getRequest() != null) {
            requestDto = RequestMapper.toRequestDto(item.getRequest());
        }

        return ItemDtoWithBookingDateResponse.builder()
                .name(item.getName())
                .description(item.getDescription())
                .owner(UserMapper.toUserDtoResponse(item.getOwner()))
                .id(item.getId())
                .request(requestDto)
                .available(item.getAvailable())
                .nextBooking(item.getNextBooking() == null ? null : BookingSimpleDto.builder()
                        .id(item.getNextBooking().getId())
                        .itemId(item.getNextBooking().getItem().getId())
                        .bookerId(item.getNextBooking().getBooker().getId())
                        .start(item.getNextBooking().getStart())
                        .end(item.getNextBooking().getEnd())
                        .build())
                .lastBooking(item.getLastBooking() == null ? null : BookingSimpleDto.builder()
                        .id(item.getLastBooking().getId())
                        .itemId(item.getLastBooking().getItem().getId())
                        .bookerId(item.getLastBooking().getBooker().getId())
                        .start(item.getLastBooking().getStart())
                        .end(item.getLastBooking().getEnd())
                        .build())
                .comments(item.getComments() == null ? null :
                    item.getComments().stream().map(CommentMapper::toCommentDtoResponse).collect(Collectors.toList()))
                .build();
    }

    public Item toItemBookingCreateRequest(Long itemId) {
        return Item.builder()
                .id(itemId)
                .build();
    }

    public List<ItemDtoResponse> toItemDtoResponseList(List<Item> items) {
        List<ItemDtoResponse> itemDtoResponseList = new ArrayList<>();
        for (Item item : items) {
            itemDtoResponseList.add(toItemDtoResponse(item));
        }
        return itemDtoResponseList;
    }
}
