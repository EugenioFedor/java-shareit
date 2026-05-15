package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.Item;

import java.util.List;

@Component
public class ItemRequestMapper {

    public ItemRequest toEntity(ItemRequestDto dto) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        return request;
    }

    public ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                items.stream()
                        .map(item -> new RequestItemDto(
                                item.getId(),
                                item.getName(),
                                item.getOwner().getId()
                        ))
                        .toList()
        );
    }
}