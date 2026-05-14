package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.CommentDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {
    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void shouldSerializeCommentDto() throws Exception {
        CommentDto dto = new CommentDto(1L, "good", "user", "2026-05-15T10:00:00");

        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.text").isEqualTo("good");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.authorName").isEqualTo("user");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.created").isEqualTo("2026-05-15T10:00:00");
    }
}
