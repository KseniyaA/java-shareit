package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingSimpleDto;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingDateResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.RequestMapper;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.UserMapper;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static Item toItemRequest(ItemDtoRequest itemDtoCreateRequest) {
        return Item.builder()
                .name(itemDtoCreateRequest.getName())
                .description(itemDtoCreateRequest.getDescription())
                .available(itemDtoCreateRequest.getAvailable())
                .build();
    }

    public static Item toItemRequest(ItemDtoRequest itemDtoRequest, Long itemId) {
        return Item.builder()
                .id(itemId)
                .name(itemDtoRequest.getName())
                .description(itemDtoRequest.getDescription())
                .available(itemDtoRequest.getAvailable())
                .build();
    }

    public static ItemDtoResponse toItemDtoResponse(Item item) {
        RequestDto requestDto = null;
        if (item.getRequest() != null) {
            requestDto = RequestMapper.toRequestDto(item.getRequest());
        }
        return ItemDtoResponse.builder()
                .name(item.getName())
                .description(item.getDescription())
                .owner(UserMapper.toUserDto(item.getOwner()))
                .id(item.getId())
                .request(requestDto)
                .available(item.getAvailable())
                .build();
    }

    public static ItemDtoWithBookingDateResponse toItemDtoWithBookingDateResponse(Item item,
                                                                                  Booking lastBooking,
                                                                                  Booking nextBooking,
                                                                                  List<Comment> comments) {
        RequestDto requestDto = null;
        if (item.getRequest() != null) {
            requestDto = RequestMapper.toRequestDto(item.getRequest());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
                .withZone(ZoneId.of("GMT"));

        String nextBookingStartDate = nextBooking == null ? null :
                formatter.format(nextBooking.getStart());
        String nextBookingEndDate = nextBooking == null ? null :
                formatter.format(nextBooking.getEnd());
        String lastBookingStartDate = lastBooking == null ? null :
                formatter.format(lastBooking.getStart());
        String lastBookingEndDate = lastBooking == null ? null :
                formatter.format(lastBooking.getEnd());

        return ItemDtoWithBookingDateResponse.builder()
                .name(item.getName())
                .description(item.getDescription())
                .owner(UserMapper.toUserDto(item.getOwner()))
                .id(item.getId())
                .request(requestDto)
                .available(item.getAvailable())
                .nextBooking(nextBooking == null ? null : BookingSimpleDto.builder()
                        .id(nextBooking.getId())
                        .itemId(nextBooking.getItem().getId())
                        .bookerId(nextBooking.getBooker().getId())
                        .start(nextBookingStartDate)
                        .end(nextBookingEndDate)
                        .build())
                .lastBooking(lastBooking == null ? null : BookingSimpleDto.builder()
                        .id(lastBooking.getId())
                        .itemId(lastBooking.getItem().getId())
                        .bookerId(lastBooking.getBooker().getId())
                        .start(lastBookingStartDate)
                        .end(lastBookingEndDate)
                        .build())
                .comments(comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()))
                .build();
    }

    public static Item toItemBookingCreateRequest(Long itemId) {
        return Item.builder()
                .id(itemId)
                .build();
    }

    public static List<ItemDtoResponse> toItemDtoResponseList(List<Item> items) {
        List<ItemDtoResponse> itemDtoResponseList = new ArrayList<>();
        for (Item item : items) {
            itemDtoResponseList.add(toItemDtoResponse(item));
        }
        return itemDtoResponseList;
    }
}
