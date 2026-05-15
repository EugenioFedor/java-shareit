package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper requestMapper;

    @Override
    @Transactional
    public ItemRequestDto create(long userId, ItemRequestDto requestDto) {
        User requestor = getUserOrThrow(userId);

        ItemRequest request = requestMapper.toEntity(requestDto);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        return requestMapper.toDto(requestRepository.save(request), List.of());
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(long userId) {
        getUserOrThrow(userId);

        List<ItemRequest> requests = requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);

        return mapRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(long userId, int from, int size) {
        getUserOrThrow(userId);

        PageRequest pageRequest = PageRequest.of(
                from / size,
                size,
                Sort.by(Sort.Direction.DESC, "created")
        );

        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNot(userId, pageRequest);

        return mapRequestsWithItems(requests);
    }

    @Override
    public ItemRequestDto getById(long userId, long requestId) {
        getUserOrThrow(userId);

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        List<Item> items = itemRepository.findAllByRequest_Id(requestId);

        return requestMapper.toDto(request, items);
    }

    private List<ItemRequestDto> mapRequestsWithItems(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<Item>> itemsByRequestId = itemRepository.findAllByRequest_IdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> requestMapper.toDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .toList();
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}