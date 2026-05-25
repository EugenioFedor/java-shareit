package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingDto;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.NewBookingDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemRequestService requestService;

    @Test
    void shouldCreateUserWithRealDatabase() {
        UserDto user = userService.createUser(new UserDto(null, "User", "user@mail.com"));

        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo("User");
        assertThat(user.getEmail()).isEqualTo("user@mail.com");
    }

    @Test
    void shouldCreateItemWithRealDatabase() {
        UserDto owner = userService.createUser(new UserDto(null, "Owner", "owner@mail.com"));

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Power drill");
        itemDto.setAvailable(true);

        ItemDto item = itemService.createItem(owner.getId(), itemDto);

        assertThat(item.getId()).isNotNull();
        assertThat(item.getName()).isEqualTo("Drill");
        assertThat(item.getDescription()).isEqualTo("Power drill");
        assertThat(item.getAvailable()).isTrue();
    }

    @Test
    void shouldCreateRequestWithRealDatabase() {
        UserDto requestor = userService.createUser(new UserDto(null, "Requestor", "requestor@mail.com"));

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need ladder");

        ItemRequestDto request = requestService.create(requestor.getId(), requestDto);

        assertThat(request.getId()).isNotNull();
        assertThat(request.getDescription()).isEqualTo("Need ladder");
        assertThat(request.getCreated()).isNotNull();
    }

    @Test
    void shouldCreateBookingWithRealDatabase() {
        UserDto owner = userService.createUser(new UserDto(null, "Owner", "owner2@mail.com"));
        UserDto booker = userService.createUser(new UserDto(null, "Booker", "booker@mail.com"));

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Saw");
        itemDto.setDescription("Electric saw");
        itemDto.setAvailable(true);

        ItemDto item = itemService.createItem(owner.getId(), itemDto);

        NewBookingDto bookingDto = new NewBookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto booking = bookingService.createBooking(booker.getId(), bookingDto);

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getItemId()).isEqualTo(item.getId());
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}