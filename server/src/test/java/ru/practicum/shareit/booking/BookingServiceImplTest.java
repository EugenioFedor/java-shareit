package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    private BookingServiceImpl service;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);
        service = new BookingServiceImpl(bookingRepository, itemRepository, userRepository, bookingMapper);

        owner = new User(1L, "Owner", "owner@mail.com", null);
        booker = new User(2L, "Booker", "booker@mail.com", null);

        item = new Item();
        item.setId(10L);
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
    }

    @Test
    void shouldCreateBooking() {
        NewBookingDto dto = newBookingDto();

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingBookings(
                eq(10L),
                eq(BookingStatus.APPROVED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(100L);
            return booking;
        });

        BookingDto result = service.createBooking(2L, dto);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getItemId()).isEqualTo(10L);
        assertThat(result.getBooker().getId()).isEqualTo(2L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void shouldThrowWhenOwnerBooksOwnItem() {
        NewBookingDto dto = newBookingDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.createBooking(1L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenItemUnavailable() {
        item.setAvailable(false);
        NewBookingDto dto = newBookingDto();

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.createBooking(2L, dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenBookingOverlaps() {
        NewBookingDto dto = newBookingDto();

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingBookings(
                eq(10L),
                eq(BookingStatus.APPROVED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(true);

        assertThatThrownBy(() -> service.createBooking(2L, dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenBookerNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createBooking(99L, newBookingDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenItemNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createBooking(2L, newBookingDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldApproveBooking() {
        Booking booking = waitingBooking();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingDto result = service.approveBooking(1L, 100L, true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void shouldRejectBooking() {
        Booking booking = waitingBooking();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingDto result = service.approveBooking(1L, 100L, false);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void shouldThrowWhenApproveByNotOwner() {
        Booking booking = waitingBooking();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.approveBooking(2L, 100L, true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenApproveAlreadyProcessedBooking() {
        Booking booking = waitingBooking();
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.approveBooking(1L, 100L, true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenApproveMissingBooking() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approveBooking(1L, 99L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetBookingByBooker() {
        Booking booking = waitingBooking();

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        BookingDto result = service.getBooking(2L, 100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void shouldGetBookingByOwner() {
        Booking booking = waitingBooking();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        BookingDto result = service.getBooking(1L, 100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void shouldThrowWhenGetBookingByAlienUser() {
        User alien = new User(3L, "Alien", "alien@mail.com", null);
        Booking booking = waitingBooking();

        when(userRepository.findById(3L)).thenReturn(Optional.of(alien));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.getBooking(3L, 100L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetUserBookingsForAllStates() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        for (BookingState state : BookingState.values()) {
            mockUserBookingRepositoryCall(state);
            List<BookingDto> result = service.getUserBookings(2L, state, 0, 10);
            assertThat(result).hasSize(1);
        }
    }

    @Test
    void shouldGetOwnerBookingsForAllStates() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        for (BookingState state : BookingState.values()) {
            mockOwnerBookingRepositoryCall(state);
            List<BookingDto> result = service.getOwnerBookings(1L, state, 0, 10);
            assertThat(result).hasSize(1);
        }
    }

    @Test
    void shouldThrowWhenGetUserBookingsByMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserBookings(99L, BookingState.ALL, 0, 10))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenGetOwnerBookingsByMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOwnerBookings(99L, BookingState.ALL, 0, 10))
                .isInstanceOf(NotFoundException.class);
    }

    private NewBookingDto newBookingDto() {
        NewBookingDto dto = new NewBookingDto();
        dto.setItemId(10L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        return dto;
    }

    private Booking waitingBooking() {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    private List<Booking> oneBooking() {
        return List.of(waitingBooking());
    }

    private void mockUserBookingRepositoryCall(BookingState state) {
        switch (state) {
            case ALL -> when(bookingRepository.findByBooker_Id(eq(2L), any(Pageable.class)))
                    .thenReturn(oneBooking());
            case CURRENT -> when(bookingRepository.findByBooker_IdAndStartBeforeAndEndAfter(
                    eq(2L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                    .thenReturn(oneBooking());
            case PAST -> when(bookingRepository.findByBooker_IdAndEndBefore(
                    eq(2L), any(LocalDateTime.class), any(Pageable.class)))
                    .thenReturn(oneBooking());
            case FUTURE -> when(bookingRepository.findByBooker_IdAndStartAfter(
                    eq(2L), any(LocalDateTime.class), any(Pageable.class)))
                    .thenReturn(oneBooking());
            case WAITING -> when(bookingRepository.findByBooker_IdAndStatus(
                    eq(2L), eq(BookingStatus.WAITING), any(Pageable.class)))
                    .thenReturn(oneBooking());
            case REJECTED -> when(bookingRepository.findByBooker_IdAndStatus(
                    eq(2L), eq(BookingStatus.REJECTED), any(Pageable.class)))
                    .thenReturn(oneBooking());
        }
    }

    private void mockOwnerBookingRepositoryCall(BookingState state) {
        switch (state) {
            case ALL -> when(bookingRepository.findByItem_Owner_Id(eq(1L), any(Pageable.class)))
                    .thenReturn(oneBooking());
            case CURRENT -> when(bookingRepository.findByItem_Owner_IdAndStartBeforeAndEndAfter(
                    eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                    .thenReturn(oneBooking());
            case PAST -> when(bookingRepository.findByItem_Owner_IdAndEndBefore(
                    eq(1L), any(LocalDateTime.class), any(Pageable.class)))
                    .thenReturn(oneBooking());
            case FUTURE -> when(bookingRepository.findByItem_Owner_IdAndStartAfter(
                    eq(1L), any(LocalDateTime.class), any(Pageable.class)))
                    .thenReturn(oneBooking());
            case WAITING -> when(bookingRepository.findByItem_Owner_IdAndStatus(
                    eq(1L), eq(BookingStatus.WAITING), any(Pageable.class)))
                    .thenReturn(oneBooking());
            case REJECTED -> when(bookingRepository.findByItem_Owner_IdAndStatus(
                    eq(1L), eq(BookingStatus.REJECTED), any(Pageable.class)))
                    .thenReturn(oneBooking());
        }
    }
}