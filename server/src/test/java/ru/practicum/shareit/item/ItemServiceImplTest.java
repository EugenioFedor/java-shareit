package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    private ItemServiceImpl service;

    private User owner;
    private User otherUser;
    private Item item;

    @BeforeEach
    void setUp() {
        ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);
        BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);

        service = new ItemServiceImpl(
                itemRepository,
                userRepository,
                bookingRepository,
                commentRepository,
                itemMapper,
                bookingMapper,
                itemRequestRepository
        );

        owner = new User(1L, "Owner", "owner@mail.com", null);
        otherUser = new User(2L, "User", "user@mail.com", null);

        item = new Item();
        item.setId(10L);
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setTags(Set.of("tool"));
    }

    @Test
    void shouldCreateItemWithoutRequest() {
        ItemDto dto = itemDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ItemDto result = service.createItem(1L, dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Drill");
    }

    @Test
    void shouldCreateItemWithRequest() {
        ItemDto dto = itemDto();
        dto.setRequestId(5L);

        ItemRequest request = new ItemRequest();
        request.setId(5L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(5L)).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ItemDto result = service.createItem(1L, dto);

        assertThat(result.getRequestId()).isEqualTo(5L);
    }

    @Test
    void shouldThrowWhenCreateByMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createItem(99L, itemDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenCreateWithoutName() {
        ItemDto dto = itemDto();
        dto.setName(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> service.createItem(1L, dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenCreateWithoutDescription() {
        ItemDto dto = itemDto();
        dto.setDescription(" ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> service.createItem(1L, dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenCreateWithoutAvailable() {
        ItemDto dto = itemDto();
        dto.setAvailable(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> service.createItem(1L, dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenCreateWithMissingRequest() {
        ItemDto dto = itemDto();
        dto.setRequestId(99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createItem(1L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldUpdateItemFully() {
        ItemDto update = new ItemDto();
        update.setName("New");
        update.setDescription("New description");
        update.setAvailable(false);
        update.setTags(Set.of("new"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        ItemDto result = service.updateItem(1L, 10L, update);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getDescription()).isEqualTo("New description");
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getTags()).contains("new");
    }

    @Test
    void shouldUpdateItemPartially() {
        ItemDto update = new ItemDto();
        update.setName("Only name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        ItemDto result = service.updateItem(1L, 10L, update);

        assertThat(result.getName()).isEqualTo("Only name");
        assertThat(result.getDescription()).isEqualTo("Power drill");
    }

    @Test
    void shouldThrowWhenUpdateByNotOwner() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.updateItem(2L, 10L, new ItemDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenUpdateMissingItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateItem(1L, 99L, new ItemDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetItemByOwnerWithBookingsAndComments() {
        Comment comment = comment();
        Booking lastBooking = booking(LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));
        Booking nextBooking = booking(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3));

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(10L)).thenReturn(List.of(comment));
        when(bookingRepository.findByItemIdInAndStatus(List.of(10L), BookingStatus.APPROVED))
                .thenReturn(List.of(lastBooking, nextBooking));

        ItemDto result = service.getItemById(1L, 10L);

        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();
    }

    @Test
    void shouldGetItemByNotOwnerWithoutBookings() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(10L)).thenReturn(List.of());

        ItemDto result = service.getItemById(2L, 10L);

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void shouldGetOwnerItemsEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwner_Id(1L)).thenReturn(List.of());

        List<ItemDto> result = service.getOwnerItems(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetOwnerItemsWithBookingsAndComments() {
        Comment comment = comment();
        Booking lastBooking = booking(LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2));
        Booking nextBooking = booking(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3));

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwner_Id(1L)).thenReturn(List.of(item));
        when(commentRepository.findCommentsMapByItemIds(List.of(10L)))
                .thenReturn(Map.of(10L, List.of(comment)));
        when(bookingRepository.findByItemIdInAndStatus(List.of(10L), BookingStatus.APPROVED))
                .thenReturn(List.of(lastBooking, nextBooking));

        List<ItemDto> result = service.getOwnerItems(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getComments()).hasSize(1);
        assertThat(result.get(0).getLastBooking()).isNotNull();
        assertThat(result.get(0).getNextBooking()).isNotNull();
    }

    @Test
    void shouldReturnEmptySearchWhenTextBlank() {
        List<ItemDto> result = service.searchItems(1L, " ");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSearchItems() {
        when(itemRepository.search("drill")).thenReturn(List.of(item));

        List<ItemDto> result = service.searchItems(1L, "drill");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void shouldAddComment() {
        CommentDto dto = new CommentDto();
        dto.setText("Good item");

        Comment saved = comment();
        saved.setText("Good item");

        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                eq(10L),
                eq(2L),
                eq(BookingStatus.APPROVED),
                any(LocalDateTime.class)
        )).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentDto result = service.addComment(2L, 10L, dto);

        assertThat(result.getText()).isEqualTo("Good item");
    }

    @Test
    void shouldThrowWhenAddCommentWithoutPastBooking() {
        CommentDto dto = new CommentDto();
        dto.setText("Good item");

        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                eq(10L),
                eq(2L),
                eq(BookingStatus.APPROVED),
                any(LocalDateTime.class)
        )).thenReturn(false);

        assertThatThrownBy(() -> service.addComment(2L, 10L, dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenAddBlankComment() {
        CommentDto dto = new CommentDto();
        dto.setText(" ");

        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                eq(10L),
                eq(2L),
                eq(BookingStatus.APPROVED),
                any(LocalDateTime.class)
        )).thenReturn(true);

        assertThatThrownBy(() -> service.addComment(2L, 10L, dto))
                .isInstanceOf(ValidationException.class);
    }

    private ItemDto itemDto() {
        ItemDto dto = new ItemDto();
        dto.setName("Drill");
        dto.setDescription("Power drill");
        dto.setAvailable(true);
        dto.setTags(Set.of("tool"));
        return dto;
    }

    private Booking booking(LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(otherUser);
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }

    private Comment comment() {
        Comment comment = new Comment();
        comment.setId(50L);
        comment.setText("Nice");
        comment.setItem(item);
        comment.setAuthor(otherUser);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }
}