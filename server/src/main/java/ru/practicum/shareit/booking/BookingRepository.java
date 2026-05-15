package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_Id(Long bookerId, Pageable pageable);

    List<Booking> findByBooker_IdAndStartBeforeAndEndAfter(
            Long bookerId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    List<Booking> findByBooker_IdAndEndBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBooker_IdAndStartAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBooker_IdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItem_Owner_Id(Long ownerId, Pageable pageable);

    List<Booking> findByItem_Owner_IdAndStartBeforeAndEndAfter(
            Long ownerId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    List<Booking> findByItem_Owner_IdAndEndBefore(Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItem_Owner_IdAndStartAfter(Long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItem_Owner_IdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    @Query("""
            select count(b) > 0
            from Booking b
            where b.item.id = :itemId
              and b.status = :status
              and b.start < :end
              and b.end > :start
            """)
    boolean existsOverlappingBookings(
            Long itemId,
            BookingStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Booking> findByItemIdInAndStatus(Collection<Long> itemIds, BookingStatus status);

    @Query("""
        select count(b) > 0
        from Booking b
        where b.item.id = :itemId
          and b.booker.id = :bookerId
          and b.end <= :now
        """)
    boolean existsCompletedBooking(
            @Param("itemId") Long itemId,
            @Param("bookerId") Long bookerId,
            @Param("now") LocalDateTime now
    );
}
