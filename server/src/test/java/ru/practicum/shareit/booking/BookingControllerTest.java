package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createShouldReturnBooking() throws Exception {
        NewBookingDto request = new NewBookingDto();
        request.setItemId(2L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto response = bookingDto(1L, request.getStart(), request.getEnd(), BookingStatus.WAITING);
        when(bookingService.createBooking(eq(1L), any(NewBookingDto.class))).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void createShouldRejectInvalidDates() throws Exception {
        NewBookingDto request = new NewBookingDto();
        request.setItemId(2L);
        request.setStart(LocalDateTime.now().plusDays(2));
        request.setEnd(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveShouldReturnBooking() throws Exception {
        when(bookingService.approveBooking(1L, 2L, true)).thenReturn(bookingDto(2L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.APPROVED));

        mockMvc.perform(patch("/bookings/2")
                        .header(USER_ID_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).approveBooking(1L, 2L, true);
    }

    @Test
    void getShouldReturnBooking() throws Exception {
        when(bookingService.getBooking(1L, 2L)).thenReturn(bookingDto(2L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.APPROVED));

        mockMvc.perform(get("/bookings/2").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void getUserBookingsShouldReturnList() throws Exception {
        when(bookingService.getUserBookings(1L, BookingState.ALL, 0, 10))
                .thenReturn(List.of(bookingDto(2L, LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2), BookingStatus.APPROVED)));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void getOwnerBookingsShouldReturnList() throws Exception {
        when(bookingService.getOwnerBookings(1L, BookingState.WAITING, 0, 10))
                .thenReturn(List.of(bookingDto(2L, LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2), BookingStatus.WAITING)));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "WAITING")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    private BookingDto bookingDto(Long id, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        return new BookingDto(id, start, end, 2L, new ItemShortDto(2L, "item"),
                new UserDto(1L, "booker", "booker@mail.com"), status);
    }
}
