package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    private final Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingDto createBooking(long userId, NewBookingDto bookingDto) {
        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingDto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Owner cannot book own item");
        }

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Item is not available");
        }

        if (bookingRepository.existsOverlappingBookings(
                item.getId(),
                BookingStatus.APPROVED,
                bookingDto.getStart(),
                bookingDto.getEnd()
        )) {
            throw new ValidationException("Booking dates overlap with existing booking");
        }

        Booking booking = bookingMapper.toBooking(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approveBooking(long userId, long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Only owner can approve booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking has already been processed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getBooking(long userId, long bookingId) {
        getUserOrThrow(userId);
        Booking booking = getBookingOrThrow(bookingId);

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Booking not available for user");
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(long userId, BookingState state) {
        getUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBooker_Id(userId, sortByStartDesc);
            case CURRENT ->
                    bookingRepository.findByBooker_IdAndStartBeforeAndEndAfter(userId, now, now, sortByStartDesc);
            case PAST -> bookingRepository.findByBooker_IdAndEndBefore(userId, now, sortByStartDesc);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartAfter(userId, now, sortByStartDesc);
            case WAITING -> bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.WAITING, sortByStartDesc);
            case REJECTED ->
                    bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, sortByStartDesc);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getOwnerBookings(long userId, BookingState state) {
        getUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItem_Owner_Id(userId, sortByStartDesc);
            case CURRENT ->
                    bookingRepository.findByItem_Owner_IdAndStartBeforeAndEndAfter(userId, now, now, sortByStartDesc);
            case PAST -> bookingRepository.findByItem_Owner_IdAndEndBefore(userId, now, sortByStartDesc);
            case FUTURE -> bookingRepository.findByItem_Owner_IdAndStartAfter(userId, now, sortByStartDesc);
            case WAITING ->
                    bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING, sortByStartDesc);
            case REJECTED ->
                    bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED, sortByStartDesc);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .toList();
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Item getItemOrThrow(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
    }

    private Booking getBookingOrThrow(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
    }
}