package ru.practicum.item;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
class ItemRepositoryImpl implements ItemRepository {

    private final List<Item> items = new ArrayList<>();
    private long currentId = 1;

    @Override
    public List<Item> findByUserId(long userId) {
        List<Item> result = new ArrayList<>();

        for (Item item : items) {
            if (item.getUserId().equals(userId)) {
                result.add(item);
            }
        }

        return result;
    }

    @Override
    public Item save(Item item) {
        item.setId(currentId);
        currentId++;
        items.add(item);
        return item;
    }

    @Override
    public void deleteByUserIdAndItemId(long userId, long itemId) {
        items.removeIf(item ->
                item.getUserId().equals(userId)
                        && item.getId().equals(itemId)
        );
    }
}