package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createShouldReturnItem() throws Exception {
        ItemDto request = new ItemDto(null, "item", "description", true, null, null, null, null, null);
        ItemDto response = new ItemDto(1L, "item", "description", true, null, null, null, null, null);

        when(itemService.createItem(eq(1L), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }


    @Test
    void updateShouldReturnUpdatedItem() throws Exception {
        ItemDto request = new ItemDto(null, "new", null, null, null, null, null, null, null);
        ItemDto response = new ItemDto(2L, "new", "description", true, null, null, null, null, null);

        when(itemService.updateItem(eq(1L), eq(2L), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(patch("/items/2")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new"));
    }

    @Test
    void getByIdShouldReturnItem() throws Exception {
        when(itemService.getItemById(1L, 2L))
                .thenReturn(new ItemDto(2L, "item", "description", true, null, null, null, null, null));

        mockMvc.perform(get("/items/2").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void getOwnerItemsShouldReturnItems() throws Exception {
        when(itemService.getOwnerItems(1L))
                .thenReturn(List.of(new ItemDto(2L, "item", "description", true, null, null, null, null, null)));

        mockMvc.perform(get("/items").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void searchShouldReturnItems() throws Exception {
        when(itemService.searchItems(1L, "drill"))
                .thenReturn(List.of(new ItemDto(1L, "drill", "description", true, null, null, null, null, null)));

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1)
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("drill"));
    }

    @Test
    void addCommentShouldReturnComment() throws Exception {
        CommentDto request = new CommentDto(null, "good", null, null);
        CommentDto response = new CommentDto(1L, "good", "user", "2026-05-15T10:00:00");

        when(itemService.addComment(eq(1L), eq(2L), any(CommentDto.class))).thenReturn(response);

        mockMvc.perform(post("/items/2/comment")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorName").value("user"));
    }
}
