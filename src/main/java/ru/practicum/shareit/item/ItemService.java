package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    ItemDto getItemById(long userId, long itemId);

    List<ItemDto> getOwnerItems(long userId);

    List<ItemDto> searchItems(long userId, String text);
}
