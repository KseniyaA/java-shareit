package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/items")
public class ItemController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private static final String ITEM_ID = "itemId";
    private final ItemService itemService;

    @PostMapping
    public ItemDtoResponse add(@RequestHeader(X_SHARER_USER_ID) long userId,
                               @RequestBody ItemDtoRequest itemDto) {
        log.info("Получен запрос POST /items с параметрами userId = {}, dto = {}", userId, itemDto);
        Item createdItem = itemService.add(ItemMapper.toItemRequest(itemDto), userId);
        return ItemMapper.toItemDtoResponse(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoResponse update(@RequestHeader(X_SHARER_USER_ID) long userId,
                                  @PathVariable(ITEM_ID) Long id,
                                  @RequestBody ItemDtoRequest itemDtoRequest) {
        log.info("Получен запрос PATCH /items/itemId с параметрами userId = {}, itemId = {}, dto = {}",
                userId, id, itemDtoRequest);
        Item updatedItem =  itemService.update(ItemMapper.toItemRequest(itemDtoRequest, id), userId);
        return ItemMapper.toItemDtoResponse(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithBookingDateResponse get(@RequestHeader(X_SHARER_USER_ID) long userId,
                                              @PathVariable(ITEM_ID) long id) {
        log.info("Получен запрос GET /items/itemId с параметрами userId = {}, itemId = {}", userId, id);
        Item item = itemService.get(id, userId);
        return ItemMapper.toItemDtoWithBookingDateResponse(item);
    }

    @GetMapping
    public List<ItemDtoWithBookingDateResponse> getAllByUser(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                             @RequestParam(required = false) Integer from,
                                                             @RequestParam(required = false) Integer size) {
        log.info("Получен запрос GET /items с параметрами userId = {}, from = {}, size = {}", userId, from, size);
        List<Item> items = itemService.getAllByUser(userId, from, size);
        return items.stream().map(ItemMapper::toItemDtoWithBookingDateResponse).collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> search(@RequestHeader(X_SHARER_USER_ID) long userId,
                                        @RequestParam String text,
                                        @RequestParam(required = false) Integer from,
                                        @RequestParam(required = false) Integer size) {
        log.info("Получен запрос GET /items/search с параметрами userId = {}, text = {}, from = {}, size = {}",
                userId, text, from, size);
        List<Item> items = itemService.searchByText(text, from, size);
        return ItemMapper.toItemDtoResponseList(items);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoResponse createComment(@RequestHeader(X_SHARER_USER_ID) long userId,
                                            @PathVariable(ITEM_ID) long itemId,
                                            @RequestBody CommentDtoRequest commentDtoRequest) {
        log.info("Получен запрос POST /items/itemId/comment с параметрами userId = {}, itemId = {}, dto = {}",
                userId, itemId, commentDtoRequest);
        Comment newComment = itemService.createComment(CommentMapper.toComment(commentDtoRequest), userId, itemId);
        return CommentMapper.toCommentDtoResponse(newComment);
    }
}
