package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void createShouldReturnRequest() throws Exception {
        ItemRequestDto request = new ItemRequestDto(null, "Need ladder", null, null);
        ItemRequestDto response = new ItemRequestDto(1L, "Need ladder", LocalDateTime.now(), List.of());

        when(requestService.create(eq(1L), any(ItemRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOwnRequestsShouldReturnList() throws Exception {
        when(requestService.getOwnRequests(1L)).thenReturn(List.of(new ItemRequestDto(1L, "Need ladder",
                LocalDateTime.now(), List.of())));

        mockMvc.perform(get("/requests").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllRequestsShouldReturnList() throws Exception {
        when(requestService.getAllRequests(1L, 0, 10)).thenReturn(List.of(new ItemRequestDto(1L, "Need ladder",
                LocalDateTime.now(), List.of(new RequestItemDto(2L, "Ladder", 3L)))));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].items[0].name").value("Ladder"));

        verify(requestService).getAllRequests(1L, 0, 10);
    }

    @Test
    void getByIdShouldReturnRequestWithItems() throws Exception {
        when(requestService.getById(1L, 2L)).thenReturn(new ItemRequestDto(2L, "Need ladder",
                LocalDateTime.now(), List.of(new RequestItemDto(3L, "Ladder", 4L))));

        mockMvc.perform(get("/requests/2").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].ownerId").value(4));
    }
}
