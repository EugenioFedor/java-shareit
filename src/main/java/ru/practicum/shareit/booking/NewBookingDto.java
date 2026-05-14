package ru.practicum.shareit.booking;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NewBookingDto {
    @NotNull
    @Future
    private LocalDateTime start;

    @NotNull
    @Future
    private LocalDateTime end;

    @NotNull
    private Long itemId;

    @AssertTrue(message = "Booking start must be before end")
    public boolean isStartBeforeEnd() {
        return start == null || end == null || start.isBefore(end);
    }
}