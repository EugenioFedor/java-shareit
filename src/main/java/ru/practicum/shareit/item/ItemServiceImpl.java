package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto createItem(long userId, ItemDto itemDto) {
        User owner = getUserOrThrow(userId);
        validateItemForCreate(itemDto);

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        if (item.getOwner() == null || !item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only owner can update item");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return itemMapper.toItemDto(itemRepository.update(item));
    }

    @Override
    public ItemDto getItemById(long userId, long itemId) {
        getUserOrThrow(userId);
        return itemMapper.toItemDto(getItemOrThrow(itemId));
    }

    @Override
    public List<ItemDto> getOwnerItems(long userId) {
        getUserOrThrow(userId);
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(long userId, String text) {
        getUserOrThrow(userId);
        return itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Item getItemOrThrow(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
    }

    private void validateItemForCreate(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Item name is required");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Item description is required");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Item available is required");
        }
    }
}