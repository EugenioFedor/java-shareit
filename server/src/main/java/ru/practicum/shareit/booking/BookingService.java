package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(long userId, NewBookingDto bookingDto);

    BookingDto approveBooking(long userId, long bookingId, boolean approved);

    BookingDto getBooking(long userId, long bookingId);

    List<BookingDto> getUserBookings(long userId, BookingState state, int from, int size);

    List<BookingDto> getOwnerBookings(long userId, BookingState state, int from, int size);
}
