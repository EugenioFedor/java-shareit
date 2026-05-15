package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    private ItemRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ItemRequestServiceImpl(
                requestRepository,
                itemRepository,
                userRepository,
                new ItemRequestMapper()
        );
    }

    @Test
    void shouldCreateRequest() {
        User user = new User(1L, "User", "user@mail.com", null);
        ItemRequestDto dto = new ItemRequestDto(null, "Need ladder", null, null);

        ItemRequest saved = new ItemRequest();
        saved.setId(10L);
        saved.setDescription("Need ladder");
        saved.setRequestor(user);
        saved.setCreated(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(saved);

        ItemRequestDto result = service.create(1L, dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getDescription()).isEqualTo("Need ladder");
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void shouldThrowWhenCreateByMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ItemRequestDto dto = new ItemRequestDto(null, "Need ladder", null, null);

        assertThatThrownBy(() -> service.create(99L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetOwnRequestsWithItems() {
        User owner = new User(2L, "Owner", "owner@mail.com", null);
        ItemRequest request = request(10L);

        Item item = new Item();
        item.setId(20L);
        item.setName("Ladder");
        item.setRequest(request);
        item.setOwner(owner);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "User", "user@mail.com", null)));
        when(requestRepository.findAllByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(request));
        when(itemRepository.findAllByRequest_IdIn(List.of(10L)))
                .thenReturn(List.of(item));

        List<ItemRequestDto> result = service.getOwnRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(0).getItems().get(0).getOwnerId()).isEqualTo(2L);
    }

    @Test
    void shouldGetOwnRequestsEmpty() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "User", "user@mail.com", null)));
        when(requestRepository.findAllByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of());
        when(itemRepository.findAllByRequest_IdIn(List.of()))
                .thenReturn(List.of());

        List<ItemRequestDto> result = service.getOwnRequests(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldGetAllRequests() {
        ItemRequest request = request(10L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "User", "user@mail.com", null)));
        when(requestRepository.findAllByRequestorIdNot(org.mockito.ArgumentMatchers.eq(1L), any(Pageable.class)))
                .thenReturn(List.of(request));
        when(itemRepository.findAllByRequest_IdIn(List.of(10L)))
                .thenReturn(List.of());

        List<ItemRequestDto> result = service.getAllRequests(1L, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
    }

    @Test
    void shouldGetById() {
        ItemRequest request = request(10L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "User", "user@mail.com", null)));
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequest_Id(10L)).thenReturn(List.of());

        ItemRequestDto result = service.getById(1L, 10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void shouldThrowWhenGetByIdMissingRequest() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "User", "user@mail.com", null)));
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(1L, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    private ItemRequest request(long id) {
        ItemRequest request = new ItemRequest();
        request.setId(id);
        request.setDescription("Need ladder");
        request.setCreated(LocalDateTime.of(2026, 5, 15, 10, 0));
        return request;
    }
}