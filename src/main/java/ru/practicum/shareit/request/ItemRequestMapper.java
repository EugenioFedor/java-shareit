package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;

@Component
public class ItemRequestMapper {

    public ItemRequestDto toItemRequestDto(ItemRequest request) {
        if (request == null) {
            return null;
        }

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());

        if (request.getRequestor() != null) {
            dto.setRequestorId(request.getRequestor().getId());
        }

        return dto;
    }

    public ItemRequest toItemRequest(ItemRequestDto dto) {
        if (dto == null) {
            return null;
        }

        ItemRequest request = new ItemRequest();
        request.setId(dto.getId());
        request.setDescription(dto.getDescription());
        request.setCreated(dto.getCreated());

        if (dto.getRequestorId() != null) {
            User requestor = new User();
            requestor.setId(dto.getRequestorId());
            request.setRequestor(requestor);
        }

        return request;
    }
}