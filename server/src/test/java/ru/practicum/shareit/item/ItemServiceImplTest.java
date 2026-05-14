package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
    private ItemMapper itemMapper;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createShouldSaveItemWithoutRequest() {
        User owner = new User(1L, "Owner", "owner@mail.com", null);

        ItemDto request = new ItemDto();
        request.setName("Drill");
        request.setDescription("Powerful drill");
        request.setAvailable(true);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);

        Item saved = new Item();
        saved.setId(10L);
        saved.setName("Drill");
        saved.setDescription("Powerful drill");
        saved.setAvailable(true);
        saved.setOwner(owner);

        ItemDto expected = new ItemDto();
        expected.setId(10L);
        expected.setName("Drill");

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(request)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(saved);
        when(itemMapper.toItemDto(saved)).thenReturn(expected);

        ItemDto result = itemService.createItem(1L, request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(item.getOwner()).isEqualTo(owner);
        verify(itemRepository).save(item);
    }

    @Test
    void createShouldSaveItemWithRequest() {
        User owner = new User(1L, "Owner", "owner@mail.com", null);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(5L);

        ItemDto request = new ItemDto();
        request.setName("Ladder");
        request.setDescription("Tall ladder");
        request.setAvailable(true);
        request.setRequestId(5L);

        Item item = new Item();

        Item saved = new Item();
        saved.setId(10L);
        saved.setRequest(itemRequest);

        ItemDto expected = new ItemDto();
        expected.setId(10L);
        expected.setRequestId(5L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(request)).thenReturn(item);
        when(itemRequestRepository.findById(5L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(item)).thenReturn(saved);
        when(itemMapper.toItemDto(saved)).thenReturn(expected);

        ItemDto result = itemService.createItem(1L, request);

        assertThat(result.getRequestId()).isEqualTo(5L);
        assertThat(item.getRequest()).isEqualTo(itemRequest);
    }

    @Test
    void createShouldThrowWhenNameBlank() {
        ItemDto request = new ItemDto();
        request.setName("");
        request.setDescription("Description");
        request.setAvailable(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> itemService.createItem(1L, request))
                .isInstanceOf(ValidationException.class);

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateShouldPatchOwnerItem() {
        User owner = new User(1L, "Owner", "owner@mail.com", null);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);
        item.setName("Old");
        item.setDescription("Old description");
        item.setAvailable(true);

        ItemDto patch = new ItemDto();
        patch.setName("New");

        ItemDto expected = new ItemDto();
        expected.setId(10L);
        expected.setName("New");

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(expected);

        ItemDto result = itemService.updateItem(1L, 10L, patch);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(item.getDescription()).isEqualTo("Old description");
    }

    @Test
    void updateShouldThrowWhenUserIsNotOwner() {
        User owner = new User(1L, "Owner", "owner@mail.com", null);
        User another = new User(2L, "Another", "another@mail.com", null);

        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        when(userRepository.findById(2L)).thenReturn(Optional.of(another));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.updateItem(2L, 10L, new ItemDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getItemByIdShouldReturnItemWithComments() {
        User owner = new User(1L, "Owner", "owner@mail.com", null);
        Item item = new Item();
        item.setId(10L);
        item.setOwner(owner);

        ItemDto dto = new ItemDto();
        dto.setId(10L);

        Comment comment = new Comment();
        comment.setId(100L);

        CommentDto commentDto = new CommentDto();
        commentDto.setId(100L);
        commentDto.setText("Nice");

        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(dto);
        when(commentRepository.findByItemId(10L)).thenReturn(List.of(comment));
        when(itemMapper.toCommentDto(comment)).thenReturn(commentDto);

        ItemDto result = itemService.getItemById(2L, 10L);

        assertThat(result.getComments()).hasSize(1);
    }

    @Test
    void getOwnerItemsShouldReturnEmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findAllByOwner_Id(1L)).thenReturn(List.of());

        List<ItemDto> result = itemService.getOwnerItems(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void searchShouldReturnEmptyWhenTextBlank() {
        List<ItemDto> result = itemService.searchItems(1L, " ");

        assertThat(result).isEmpty();
        verify(itemRepository, never()).search(anyString());
    }

    @Test
    void searchShouldReturnMappedItems() {
        Item item = new Item();
        item.setId(10L);

        ItemDto dto = new ItemDto();
        dto.setId(10L);

        when(itemRepository.search("drill")).thenReturn(List.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(dto);

        List<ItemDto> result = itemService.searchItems(1L, "drill");

        assertThat(result).hasSize(1);
    }

    @Test
    void addCommentShouldSaveCommentWhenUserHasPastBooking() {
        User author = new User(2L, "Booker", "booker@mail.com", null);
        Item item = new Item();
        item.setId(10L);

        CommentDto request = new CommentDto();
        request.setText("Good item");

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setText("Good item");

        CommentDto expected = new CommentDto();
        expected.setId(100L);
        expected.setText("Good item");

        when(userRepository.findById(2L)).thenReturn(Optional.of(author));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                eq(10L),
                eq(2L),
                eq(BookingStatus.APPROVED),
                any(LocalDateTime.class)
        )).thenReturn(true);
        when(itemMapper.toComment(request, item, author)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(itemMapper.toCommentDto(comment)).thenReturn(expected);

        CommentDto result = itemService.addComment(2L, 10L, request);

        assertThat(result.getText()).isEqualTo("Good item");
    }

    @Test
    void addCommentShouldThrowWithoutPastBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(new Item()));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                eq(10L),
                eq(2L),
                eq(BookingStatus.APPROVED),
                any(LocalDateTime.class)
        )).thenReturn(false);

        CommentDto request = new CommentDto();
        request.setText("Good item");

        assertThatThrownBy(() -> itemService.addComment(2L, 10L, request))
                .isInstanceOf(ValidationException.class);
    }
}