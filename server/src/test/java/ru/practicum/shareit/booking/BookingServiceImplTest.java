package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBookingShouldSaveWaitingBooking() {
        User owner = user(1L);
        User booker = user(2L);
        Item item = item(10L, owner, true);

        NewBookingDto request = new NewBookingDto();
        request.setItemId(10L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        Booking booking = booking(item, booker, BookingStatus.WAITING);
        BookingDto dto = new BookingDto();
        dto.setId(100L);
        dto.setStatus(BookingStatus.WAITING);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingBookings(eq(10L), eq(BookingStatus.APPROVED), any(), any()))
                .thenReturn(false);
        when(bookingMapper.toBooking(request, item, booker)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toBookingDto(booking)).thenReturn(dto);

        BookingDto result = bookingService.createBooking(2L, request);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(bookingRepository).save(booking);
    }

    @Test
    void createBookingShouldThrowWhenOwnerBooksOwnItem() {
        User owner = user(1L);
        NewBookingDto request = bookingRequest(10L);
        Item item = item(10L, owner, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(1L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createBookingShouldThrowWhenItemUnavailable() {
        User owner = user(1L);
        User booker = user(2L);
        NewBookingDto request = bookingRequest(10L);
        Item item = item(10L, owner, false);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(2L, request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createBookingShouldThrowWhenDatesOverlap() {
        User owner = user(1L);
        User booker = user(2L);
        NewBookingDto request = bookingRequest(10L);
        Item item = item(10L, owner, true);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingBookings(eq(10L), eq(BookingStatus.APPROVED), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(2L, request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approveBookingShouldApproveWaitingBooking() {
        User owner = user(1L);
        User booker = user(2L);
        Item item = item(10L, owner, true);
        Booking booking = booking(item, booker, BookingStatus.WAITING);

        BookingDto dto = new BookingDto();
        dto.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toBookingDto(booking)).thenReturn(dto);

        BookingDto result = bookingService.approveBooking(1L, 100L, true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveBookingShouldThrowWhenUserIsNotOwner() {
        User owner = user(1L);
        User another = user(3L);
        User booker = user(2L);
        Item item = item(10L, owner, true);
        Booking booking = booking(item, booker, BookingStatus.WAITING);

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(another.getId(), 100L, true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approveBookingShouldThrowWhenAlreadyProcessed() {
        User owner = user(1L);
        User booker = user(2L);
        Item item = item(10L, owner, true);
        Booking booking = booking(item, booker, BookingStatus.APPROVED);

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 100L, true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getBookingShouldAllowBooker() {
        User owner = user(1L);
        User booker = user(2L);
        Booking booking = booking(item(10L, owner, true), booker, BookingStatus.WAITING);

        BookingDto dto = new BookingDto();
        dto.setId(100L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(dto);

        BookingDto result = bookingService.getBooking(2L, 100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void getBookingShouldThrowForForeignUser() {
        User owner = user(1L);
        User booker = user(2L);
        User foreign = user(3L);
        Booking booking = booking(item(10L, owner, true), booker, BookingStatus.WAITING);

        when(userRepository.findById(3L)).thenReturn(Optional.of(foreign));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBooking(3L, 100L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserBookingsShouldReturnAll() {
        User booker = user(2L);
        Booking booking = new Booking();
        booking.setId(100L);

        BookingDto dto = new BookingDto();
        dto.setId(100L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBooker_Id(eq(2L), any(Pageable.class))).thenReturn(List.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(dto);

        List<BookingDto> result = bookingService.getUserBookings(2L, BookingState.ALL, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }

    @Test
    void getOwnerBookingsShouldReturnWaiting() {
        User owner = user(1L);
        Booking booking = new Booking();
        booking.setId(100L);

        BookingDto dto = new BookingDto();
        dto.setId(100L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_Owner_IdAndStatus(eq(1L), eq(BookingStatus.WAITING), any(Pageable.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(dto);

        List<BookingDto> result = bookingService.getOwnerBookings(1L, BookingState.WAITING, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }

    private User user(Long id) {
        return new User(id, "User " + id, "user" + id + "@mail.com", null);
    }

    private Item item(Long id, User owner, Boolean available) {
        Item item = new Item();
        item.setId(id);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(available);
        item.setOwner(owner);
        return item;
    }

    private Booking booking(Item item, User booker, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(status);
        return booking;
    }

    private NewBookingDto bookingRequest(Long itemId) {
        NewBookingDto request = new NewBookingDto();
        request.setItemId(itemId);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));
        return request;
    }
}