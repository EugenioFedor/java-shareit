package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.RequestItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeRequestDtoWithCreatedDateAndItems() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(
                1L,
                "Need ladder",
                LocalDateTime.of(2026, 5, 15, 10, 30),
                List.of(new RequestItemDto(2L, "Ladder", 3L))
        );

        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.description").isEqualTo("Need ladder");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.created").isEqualTo("2026-05-15T10:30:00");
        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(2);
        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(3);
    }

    @Test
    void shouldDeserializeRequestDtoWithCreatedDate() throws Exception {
        String content = "{\"id\":1,"
                + "\"description\":\"Need ladder\","
                + "\"created\":\"2026-05-15T10:30:00\","
                + "\"items\":[]}";

        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 5, 15, 10, 30));
        assertThat(dto.getItems()).isEmpty();
    }
}
