package ru.practicum.shareit.item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    List<Item> findAllByOwnerId(long ownerId);

    Optional<Item> findById(long itemId);

    Item save(Item item);

    Item update(Item item);

    List<Item> search(String text);
}
