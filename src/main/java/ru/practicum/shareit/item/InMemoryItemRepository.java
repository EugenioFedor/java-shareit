package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long currentId = 1;

    @Override
    public List<Item> findAllByOwnerId(long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null && item.getOwner().getId().equals(ownerId))
                .toList();
    }

    @Override
    public Optional<Item> findById(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Item save(Item item) {
        item.setId(currentId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        String query = text.toLowerCase(Locale.ROOT);
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> containsIgnoreCase(item.getName(), query)
                        || containsIgnoreCase(item.getDescription(), query))
                .toList();
    }

    private boolean containsIgnoreCase(String source, String query) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(query);
    }
}
