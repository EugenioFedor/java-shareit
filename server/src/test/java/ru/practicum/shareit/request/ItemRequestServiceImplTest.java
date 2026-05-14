package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestMapper requestMapper;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Test
    void createShouldSaveRequest() {
        User requestor = new User(1L, "User", "user@mail.com", null);

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need ladder");

        ItemRequest saved = new ItemRequest();
        saved.setId(10L);
        saved.setDescription("Need ladder");
        saved.setRequestor(requestor);
        saved.setCreated(LocalDateTime.now());

        ItemRequestDto expected = new ItemRequestDto();
        expected.setId(10L);
        expected.setDescription("Need ladder");

        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(saved);
        when(requestMapper.toDto(saved, List.of())).thenReturn(expected);

        ItemRequestDto result = requestService.create(1L, requestDto);

        assertThat(result.getId()).isEqualTo(10L);
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void createShouldThrowWhenDescriptionBlank() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription(" ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> requestService.create(1L, requestDto))
                .isInstanceOf(ValidationException.class);

        verify(requestRepository, never()).save(any());
    }

    @Test
    void getOwnRequestsShouldReturnRequestsWithItems() {
        User user = new User(1L, "User", "user@mail.com", null);

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setRequestor(user);

        Item item = new Item();
        item.setId(20L);
        item.setRequest(request);

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(request));
        when(itemRepository.findAllByRequest_IdIn(List.of(10L))).thenReturn(List.of(item));
        when(requestMapper.toDto(request, List.of(item))).thenReturn(dto);

        List<ItemRequestDto> result = requestService.getOwnRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(10L);
    }

    @Test
    void getAllRequestsShouldReturnOtherUsersRequests() {
        User user = new User(1L, "User", "user@mail.com", null);

        ItemRequest request = new ItemRequest();
        request.setId(10L);

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequestorIdNot(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(request));
        when(itemRepository.findAllByRequest_IdIn(List.of(10L))).thenReturn(List.of());
        when(requestMapper.toDto(request, List.of())).thenReturn(dto);

        List<ItemRequestDto> result = requestService.getAllRequests(1L, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getByIdShouldReturnRequestWithItems() {
        User user = new User(1L, "User", "user@mail.com", null);

        ItemRequest request = new ItemRequest();
        request.setId(10L);

        Item item = new Item();
        item.setId(20L);

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequest_Id(10L)).thenReturn(List.of(item));
        when(requestMapper.toDto(request, List.of(item))).thenReturn(dto);

        ItemRequestDto result = requestService.getById(1L, 10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void getByIdShouldThrowWhenRequestNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getById(1L, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getOwnRequests(99L))
                .isInstanceOf(NotFoundException.class);
    }
}