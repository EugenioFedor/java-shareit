package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public ItemDto createItem(long userId, ItemDto itemDto) {
        User owner = getUserOrThrow(userId);
        validateItemForCreate(itemDto);

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        if (!item.getOwner().getId().equals(userId)) {
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
        if (itemDto.getTags() != null) {
            item.setTags(itemDto.getTags());
        }

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItemById(long userId, long itemId) {
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        ItemDto dto = itemMapper.toItemDto(item);

        dto.setComments(commentRepository.findByItemId(itemId).stream()
                .map(itemMapper::toCommentDto)
                .toList());

        if (item.getOwner().getId().equals(userId)) {
            fillBookingsForSingleItem(dto, itemId);
        }

        return dto;
    }

    @Override
    public List<ItemDto> getOwnerItems(long userId) {
        getUserOrThrow(userId);

        List<Item> items = itemRepository.findAllByOwner_Id(userId);
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        Map<Long, List<Comment>> comments = commentRepository.findCommentsMapByItemIds(itemIds);

        Map<Long, Booking> lastBookings = bookingRepository
                .findByItemIdInAndStatusAndEndBefore(
                        itemIds,
                        BookingStatus.APPROVED,
                        LocalDateTime.now(),
                        Sort.by(Sort.Direction.DESC, "end")
                )
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        Function.identity(),
                        (first, second) -> first
                ));

        Map<Long, Booking> nextBookings = bookingRepository
                .findByItemIdInAndStatusAndStartAfter(
                        itemIds,
                        BookingStatus.APPROVED,
                        LocalDateTime.now(),
                        Sort.by(Sort.Direction.ASC, "start")
                )
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        Function.identity(),
                        (first, second) -> first
                ));

        return items.stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toItemDto(item);

                    dto.setComments(comments.getOrDefault(item.getId(), List.of()).stream()
                            .map(itemMapper::toCommentDto)
                            .toList());

                    dto.setLastBooking(bookingMapper.toBookingShortDto(lastBookings.get(item.getId())));
                    dto.setNextBooking(bookingMapper.toBookingShortDto(nextBookings.get(item.getId())));

                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(long userId, String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        boolean hasPastBooking = bookingRepository
                .existsByItemIdAndBookerIdAndEndBefore(
                        itemId,
                        userId,
                        LocalDateTime.now()
                );

        if (!hasPastBooking) {
            throw new ValidationException("User has no completed booking for this item");
        }

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Comment text is required");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return itemMapper.toCommentDto(commentRepository.save(comment));
    }

    private void fillBookingsForSingleItem(ItemDto dto, long itemId) {
        LocalDateTime now = LocalDateTime.now();

        bookingRepository
                .findByItemIdInAndStatusAndEndBefore(
                        List.of(itemId),
                        BookingStatus.APPROVED,
                        now,
                        Sort.by(Sort.Direction.DESC, "end")
                )
                .stream()
                .findFirst()
                .ifPresent(booking -> dto.setLastBooking(bookingMapper.toBookingShortDto(booking)));

        bookingRepository
                .findByItemIdInAndStatusAndStartAfter(
                        List.of(itemId),
                        BookingStatus.APPROVED,
                        now,
                        Sort.by(Sort.Direction.ASC, "start")
                )
                .stream()
                .findFirst()
                .ifPresent(booking -> dto.setNextBooking(bookingMapper.toBookingShortDto(booking)));
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