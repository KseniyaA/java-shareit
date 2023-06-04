package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDtoCreateRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoUpdateRequest;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDtoResponse add(@RequestHeader("X-Sharer-User-Id") long userId,
                               @Valid @RequestBody ItemDtoCreateRequest itemDto) {
        Item createdItem = itemService.add(ItemMapper.toItemCreateRequest(itemDto), userId);
        return ItemMapper.toItemDtoResponse(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoResponse update(@RequestHeader("X-Sharer-User-Id") long userId,
                       @PathVariable("itemId") long id,
                       @RequestBody ItemDtoUpdateRequest itemDtoUpdateRequest) {
        Item updatedItem =  itemService.update(ItemMapper.toItemFromItemDtoUpdateRequest(itemDtoUpdateRequest, id), userId);
        return ItemMapper.toItemDtoResponse(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ItemDtoResponse get(@RequestHeader("X-Sharer-User-Id") long userId,
                               @PathVariable("itemId") long id) {
        return ItemMapper.toItemDtoResponse(itemService.get(id));
    }

    @GetMapping
    public List<ItemDtoResponse> getAllByUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        return ItemMapper.toItemDtoResponseList(itemService.getAllByUser(userId));
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> search(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestParam String text) {
        List<Item> items = itemService.searchByText(text);
        return ItemMapper.toItemDtoResponseList(items);
    }
}
