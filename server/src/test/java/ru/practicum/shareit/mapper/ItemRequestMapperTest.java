package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {
    private final ItemRequestMapper mapper = new ItemRequestMapper();

    @Test
    void shouldMapRequestWithItemsToDto() {
        User requestor = new User(1L, "Requestor", "requestor@mail.com", null);
        User owner = new User(2L, "Owner", "owner@mail.com", null);

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Need ladder");
        request.setCreated(LocalDateTime.of(2026, 5, 15, 10, 30));
        request.setRequestor(requestor);

        Item item = new Item();
        item.setId(20L);
        item.setName("Ladder");
        item.setOwner(owner);

        ItemRequestDto dto = mapper.toDto(request, List.of(item));

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getDescription()).isEqualTo("Need ladder");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 5, 15, 10, 30));
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(20L);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Ladder");
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(2L);
    }

    @Test
    void shouldMapRequestWithoutItemsToDto() {
        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Need ladder");
        request.setCreated(LocalDateTime.of(2026, 5, 15, 10, 30));

        ItemRequestDto dto = mapper.toDto(request, List.of());

        assertThat(dto.getItems()).isEmpty();
    }
}