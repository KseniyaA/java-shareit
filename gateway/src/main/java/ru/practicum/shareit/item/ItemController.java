package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.Marker;
import ru.practicum.shareit.common.ValidateFromIfPresent;
import ru.practicum.shareit.common.ValidateSizeIfPresent;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoRequest;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/items")
public class ItemController {
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private static final String ITEM_ID = "itemId";
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(X_SHARER_USER_ID) long userId,
                                      @RequestBody @Validated(Marker.OnCreate.class) ItemDtoRequest itemDto) {
        log.info("Получен запрос POST /items с параметрами userId = {}, dto = {}", userId, itemDto);
        return itemClient.add(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(X_SHARER_USER_ID) long userId,
                                         @PathVariable(ITEM_ID) Long id,
                                         @RequestBody @Validated(Marker.OnUpdate.class) ItemDtoRequest itemDtoRequest) {
        log.info("Получен запрос PATCH /items/itemId с параметрами userId = {}, itemId = {}, dto = {}",
                userId, id, itemDtoRequest);
        return itemClient.update(itemDtoRequest, id, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@RequestHeader(X_SHARER_USER_ID) long userId,
                                      @PathVariable(ITEM_ID) long id) {
        log.info("Получен запрос GET /items/itemId с параметрами userId = {}, itemId = {}", userId, id);
        return itemClient.get(id, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUser(@RequestHeader(X_SHARER_USER_ID) long userId,
                                               @RequestParam(required = false) @ValidateFromIfPresent Integer from,
                                               @RequestParam(required = false) @ValidateSizeIfPresent Integer size) {
        log.info("Получен запрос GET /items с параметрами userId = {}, from = {}, size = {}", userId, from, size);
        return itemClient.getAllByUser(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(X_SHARER_USER_ID) long userId,
                                         @RequestParam String text,
                                         @RequestParam(required = false) @ValidateFromIfPresent Integer from,
                                         @RequestParam(required = false) @ValidateSizeIfPresent Integer size) {
        log.info("Получен запрос GET /items/search с параметрами userId = {}, text = {}, from = {}, size = {}",
                userId, text, from, size);
        return itemClient.searchByText(text, from, size, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                @PathVariable(ITEM_ID) long itemId,
                                                @RequestBody @Validated({Marker.OnCreate.class}) CommentDtoRequest commentDtoRequest) {
        log.info("Получен запрос POST /items/itemId/comment с параметрами userId = {}, itemId = {}, dto = {}",
                userId, itemId, commentDtoRequest);
        return itemClient.createComment(commentDtoRequest, userId, itemId);
    }
}
