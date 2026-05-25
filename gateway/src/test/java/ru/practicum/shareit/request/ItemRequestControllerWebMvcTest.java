package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerWebMvcTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient requestClient;

    @Test
    void createShouldDelegateToClient() throws Exception {
        ItemRequestDto request = new ItemRequestDto(null, "Need ladder", null, null);
        ItemRequestDto response = new ItemRequestDto(1L, "Need ladder", LocalDateTime.now(), List.of());

        when(requestClient.create(eq(1L), any(ItemRequestDto.class))).thenReturn(ok(response));

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createShouldRejectBlankDescriptionBeforeClient() throws Exception {
        ItemRequestDto request = new ItemRequestDto(null, "", null, null);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    void getOwnRequestsShouldDelegateToClient() throws Exception {
        when(requestClient.getOwnRequests(1L)).thenReturn(ok(List.of(new ItemRequestDto(1L,
                "Need ladder", LocalDateTime.now(), List.of()))));

        mockMvc.perform(get("/requests").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllRequestsShouldDelegateToClientWithPaging() throws Exception {
        when(requestClient.getAllRequests(1L, 0, 10)).thenReturn(ok(List.of()));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(requestClient).getAllRequests(1L, 0, 10);
    }

    @Test
    void getAllRequestsShouldRejectWrongPagingBeforeClient() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    void getByIdShouldDelegateToClient() throws Exception {
        when(requestClient.getById(1L, 2L)).thenReturn(ok(new ItemRequestDto(2L, "Need ladder",
                LocalDateTime.now(), List.of(new RequestItemDto(3L, "Ladder", 4L)))));

        mockMvc.perform(get("/requests/2").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Ladder"));
    }

    private ResponseEntity<Object> ok(Object body) {
        return ResponseEntity.ok(body);
    }

}
