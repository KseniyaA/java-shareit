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
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @Validated(Marker.OnCreate.class) @RequestBody ItemDtoRequest itemDto) {
        return itemClient.add(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable("itemId") Long id,
                                  @Validated(Marker.OnUpdate.class) @RequestBody ItemDtoRequest itemDtoRequest) {
        return itemClient.update(itemDtoRequest, id, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @PathVariable("itemId") long id) {
        return itemClient.get(id, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUser(@RequestHeader("X-Sharer-User-Id") long userId,
                                               @RequestParam(required = false) @ValidateFromIfPresent Integer from,
                                               @RequestParam(required = false) @ValidateSizeIfPresent Integer size) {
        return itemClient.getAllByUser(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestParam String text,
                                        @RequestParam(required = false) @ValidateFromIfPresent Integer from,
                                        @RequestParam(required = false) @ValidateSizeIfPresent Integer size) {
        return itemClient.searchByText(text, from, size, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @PathVariable("itemId") long itemId,
                                                @Validated({Marker.OnCreate.class}) @RequestBody CommentDtoRequest commentDtoRequest) {
        return itemClient.createComment(commentDtoRequest, userId, itemId);
    }
}
