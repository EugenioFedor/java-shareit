package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerWebMvcTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void createShouldDelegateToClient() throws Exception {
        ItemDto request = new ItemDto(null, "item", "description", true, 10L);
        ItemDto response = new ItemDto(1L, "item", "description", true, 10L);

        when(itemClient.create(eq(1L), any(ItemDto.class))).thenReturn(ok(response));

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(10));
    }

    @Test
    void createShouldRejectInvalidItemBeforeClient() throws Exception {
        ItemDto request = new ItemDto(null, "", "description", true, null);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void updateShouldDelegateToClient() throws Exception {
        ItemDto request = new ItemDto(null, "new", null, null, null);
        ItemDto response = new ItemDto(2L, "new", "old", true, null);

        when(itemClient.update(eq(1L), eq(2L), any(ItemDto.class))).thenReturn(ok(response));

        mockMvc.perform(patch("/items/2")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new"));
    }

    @Test
    void getByIdShouldDelegateToClient() throws Exception {
        when(itemClient.getById(1L, 2L)).thenReturn(ok(new ItemDto(2L, "item", "desc", true, null)));

        mockMvc.perform(get("/items/2").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void getAllShouldDelegateToClient() throws Exception {
        when(itemClient.getAll(1L)).thenReturn(ok(List.of(new ItemDto(2L, "item", "desc", true, null))));

        mockMvc.perform(get("/items").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void searchShouldDelegateToClient() throws Exception {
        when(itemClient.search(1L, "drill"))
                .thenReturn(ok(List.of(new ItemDto(2L, "drill", "desc", true, null))));

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1)
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("drill"));
    }

    @Test
    void addCommentShouldDelegateToClient() throws Exception {
        CommentDto request = new CommentDto(null, "good", null, null);
        CommentDto response = new CommentDto(1L, "good", "user", "2026-05-15T10:00:00");

        when(itemClient.addComment(eq(1L), eq(2L), any(CommentDto.class))).thenReturn(ok(response));

        mockMvc.perform(post("/items/2/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorName").value("user"));

        verify(itemClient).addComment(eq(1L), eq(2L), any(CommentDto.class));
    }

    private ResponseEntity<Object> ok(Object body) {
        return ResponseEntity.ok(body);
    }

}
