package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerWebMvcTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createShouldDelegateToClient() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(2L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingDto response = new BookingDto(1L, request.getStart(), request.getEnd(), 2L, null, null,
                BookingStatus.WAITING);

        when(bookingClient.bookItem(eq(1L), any(BookItemRequestDto.class))).thenReturn(ok(response));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void createShouldRejectWrongDatesBeforeClient() throws Exception {
        BookItemRequestDto request = new BookItemRequestDto(2L, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getBookingShouldDelegateToClient() throws Exception {
        when(bookingClient.getBooking(1L, 2L)).thenReturn(ok(new BookingDto(2L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 3L, null, null,
                BookingStatus.APPROVED)));

        mockMvc.perform(get("/bookings/2").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void approveShouldDelegateToClient() throws Exception {
        when(bookingClient.approveBooking(1L, 2L, true)).thenReturn(ok(new BookingDto(2L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 3L, null, null,
                BookingStatus.APPROVED)));

        mockMvc.perform(patch("/bookings/2")
                        .header(USER_ID_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingClient).approveBooking(1L, 2L, true);
    }

    @Test
    void getUserBookingsShouldDelegateToClientWithPaging() throws Exception {
        when(bookingClient.getBookings(1L, BookingState.ALL, 0, 10)).thenReturn(ok(List.of()));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "all")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(bookingClient).getBookings(1L, BookingState.ALL, 0, 10);
    }

    @Test
    void getOwnerBookingsShouldDelegateToClientWithPaging() throws Exception {
        when(bookingClient.getOwnerBookings(1L, BookingState.WAITING, 0, 5)).thenReturn(ok(List.of()));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1)
                        .param("state", "waiting")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(bookingClient).getOwnerBookings(1L, BookingState.WAITING, 0, 5);
    }

    @Test
    void getBookingsShouldRejectWrongPagingBeforeClient() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    private ResponseEntity<Object> ok(Object body) {
        return ResponseEntity.ok(body);
    }

}
