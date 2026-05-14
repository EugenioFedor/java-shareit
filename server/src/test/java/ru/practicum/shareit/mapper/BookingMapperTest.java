package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {
    private final BookingMapper mapper = Mappers.getMapper(BookingMapper.class);

    @Test
    void shouldMapBookingToDto() {
        User owner = new User(1L, "Owner", "owner@mail.com", null);
        User booker = new User(2L, "Booker", "booker@mail.com", null);

        Item item = new Item();
        item.setId(10L);
        item.setName("Drill");
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStart(LocalDateTime.of(2026, 5, 15, 10, 0));
        booking.setEnd(LocalDateTime.of(2026, 5, 15, 11, 0));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);

        BookingDto dto = mapper.toBookingDto(booking);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 5, 15, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 5, 15, 11, 0));
        assertThat(dto.getItemId()).isEqualTo(10L);
        assertThat(dto.getItem().getId()).isEqualTo(10L);
        assertThat(dto.getItem().getName()).isEqualTo("Drill");
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void shouldMapNewBookingDtoToBooking() {
        User booker = new User(2L, "Booker", "booker@mail.com", null);

        Item item = new Item();
        item.setId(10L);

        NewBookingDto dto = new NewBookingDto();
        dto.setItemId(10L);
        dto.setStart(LocalDateTime.of(2026, 5, 15, 10, 0));
        dto.setEnd(LocalDateTime.of(2026, 5, 15, 11, 0));

        Booking booking = mapper.toBooking(dto, item, booker);

        assertThat(booking.getStart()).isEqualTo(LocalDateTime.of(2026, 5, 15, 10, 0));
        assertThat(booking.getEnd()).isEqualTo(LocalDateTime.of(2026, 5, 15, 11, 0));
        assertThat(booking.getItem()).isEqualTo(item);
        assertThat(booking.getBooker()).isEqualTo(booker);
    }
}