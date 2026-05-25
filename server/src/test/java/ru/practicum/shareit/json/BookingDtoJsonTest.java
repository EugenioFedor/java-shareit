package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.ItemShortDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {
    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void shouldSerializeBookingDtoWithDates() throws Exception {
        BookingDto dto = new BookingDto(
                1L,
                LocalDateTime.of(2026, 5, 15, 10, 0),
                LocalDateTime.of(2026, 5, 15, 11, 0),
                2L,
                new ItemShortDto(2L, "Drill"),
                new UserDto(3L, "Booker", "booker@mail.com"),
                BookingStatus.APPROVED
        );

        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.start").isEqualTo("2026-05-15T10:00:00");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.end").isEqualTo("2026-05-15T11:00:00");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.item.id").isEqualTo(2);
        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.booker.id").isEqualTo(3);
    }

    @Test
    void shouldDeserializeBookingDtoWithDates() throws Exception {
        String content = "{\"id\":1,"
                + "\"start\":\"2026-05-15T10:00:00\","
                + "\"end\":\"2026-05-15T11:00:00\","
                + "\"itemId\":2,"
                + "\"status\":\"WAITING\"}";

        assertThat(json.parseObject(content).getStart()).isEqualTo(LocalDateTime.of(2026, 5, 15, 10, 0));
        assertThat(json.parseObject(content).getEnd()).isEqualTo(LocalDateTime.of(2026, 5, 15, 11, 0));
        assertThat(json.parseObject(content).getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}
