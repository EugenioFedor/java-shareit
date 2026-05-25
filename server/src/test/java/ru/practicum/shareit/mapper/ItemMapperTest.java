package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {
    private final ItemMapper mapper = Mappers.getMapper(ItemMapper.class);

    @Test
    void shouldMapItemToDto() {
        ItemRequest request = new ItemRequest();
        request.setId(5L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setRequest(request);
        item.setTags(Set.of("tool"));

        ItemDto dto = mapper.toItemDto(item);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Drill");
        assertThat(dto.getDescription()).isEqualTo("Powerful drill");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(5L);
        assertThat(dto.getTags()).contains("tool");
    }

    @Test
    void shouldMapDtoToItem() {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Drill");
        dto.setDescription("Powerful drill");
        dto.setAvailable(true);
        dto.setRequestId(5L);
        dto.setTags(Set.of("tool"));

        Item item = mapper.toItem(dto);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Drill");
        assertThat(item.getDescription()).isEqualTo("Powerful drill");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getRequest()).isNull();
        assertThat(item.getTags()).contains("tool");
    }

    @Test
    void shouldMapCommentToDto() {
        User author = new User(1L, "Author", "author@mail.com", null);

        Comment comment = new Comment();
        comment.setId(10L);
        comment.setText("Nice");
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.of(2026, 5, 15, 10, 0));

        CommentDto dto = mapper.toCommentDto(comment);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getText()).isEqualTo("Nice");
        assertThat(dto.getAuthorName()).isEqualTo("Author");
    }

    @Test
    void shouldMapCommentDtoToComment() {
        Item item = new Item();
        item.setId(1L);

        User author = new User(2L, "Author", "author@mail.com", null);

        CommentDto dto = new CommentDto();
        dto.setText("Nice");

        Comment comment = mapper.toComment(dto, item, author);

        assertThat(comment.getText()).isEqualTo("Nice");
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getCreated()).isNotNull();
    }
}