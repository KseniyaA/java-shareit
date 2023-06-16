package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.Marker;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDtoResponse add(@RequestHeader("X-Sharer-User-Id") long userId,
                               @Validated(Marker.OnCreate.class) @RequestBody ItemDtoRequest itemDto) {
        Item createdItem = itemService.add(ItemMapper.toItemRequest(itemDto), userId);
        return ItemMapper.toItemDtoResponse(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoResponse update(@RequestHeader("X-Sharer-User-Id") long userId,
                       @PathVariable("itemId") Long id,
                       @Validated(Marker.OnUpdate.class) @RequestBody ItemDtoRequest itemDtoRequest) {
        Item updatedItem =  itemService.update(ItemMapper.toItemRequest(itemDtoRequest, id), userId);
        return ItemMapper.toItemDtoResponse(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithBookingDateResponse get(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @PathVariable("itemId") long id) {
        Item item = itemService.get(id, userId);
        return ItemMapper.toItemDtoWithBookingDateResponse(item);
    }

    @GetMapping
    public List<ItemDtoWithBookingDateResponse> getAllByUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        List<Item> items = itemService.getAllByUser(userId);
        return items.stream().map(ItemMapper::toItemDtoWithBookingDateResponse).collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> search(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestParam String text) {
        List<Item> items = itemService.searchByText(text);
        return ItemMapper.toItemDtoResponseList(items);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoResponse createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                            @PathVariable("itemId") long itemId,
                                            @Validated({Marker.OnCreate.class}) @RequestBody CommentDtoRequest commentDtoRequest) {
        Comment newComment = itemService.createComment(CommentMapper.toComment(commentDtoRequest), userId, itemId);
        return CommentMapper.toCommentDtoResponse(newComment);
    }
}
