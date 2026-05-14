package ru.practicum.shareit.request;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(long userId, ItemRequestDto requestDto);

    List<ItemRequestDto> getOwnRequests(long userId);

    List<ItemRequestDto> getAllRequests(long userId, int from, int size);

    ItemRequestDto getById(long userId, long requestId);
}