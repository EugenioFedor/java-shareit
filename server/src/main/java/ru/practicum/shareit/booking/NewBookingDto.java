package ru.practicum.shareit.booking;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NewBookingDto {
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
}