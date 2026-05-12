package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_Id(Long bookerId, Sort sort);

    List<Booking> findByBooker_IdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByBooker_IdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBooker_IdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBooker_IdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByItem_Owner_Id(Long ownerId, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByItem_Owner_IdAndEndBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItem_Owner_IdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    List<Booking> findByItemIdAndStatusAndEndBefore(Long itemId, BookingStatus status, LocalDateTime end);

    List<Booking> findByItemIdInAndStatusAndEndBefore(Collection<Long> itemIds, BookingStatus status, LocalDateTime end, Sort sort);

    List<Booking> findByItemIdInAndStatusAndStartAfter(Collection<Long> itemIds, BookingStatus status, LocalDateTime start, Sort sort);

    boolean existsByItemIdAndBookerIdAndEndBefore(
            Long itemId,
            Long bookerId,
            LocalDateTime end
    );
}