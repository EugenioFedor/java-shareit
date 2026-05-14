package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void createShouldDelegateToClient() throws Exception {
        UserDto request = new UserDto(null, "user", "user@mail.com");
        UserDto response = new UserDto(1L, "user", "user@mail.com");

        when(userClient.create(any(UserDto.class))).thenReturn(ok(response));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@mail.com"));

        verify(userClient).create(any(UserDto.class));
    }

    @Test
    void createShouldRejectInvalidEmailBeforeClient() throws Exception {
        UserDto request = new UserDto(null, "user", "wrong-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void updateShouldDelegateToClient() throws Exception {
        UserDto request = new UserDto(null, "new", null);
        UserDto response = new UserDto(1L, "new", "old@mail.com");

        when(userClient.update(eq(1L), any(UserDto.class))).thenReturn(ok(response));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new"));
    }

    @Test
    void getByIdShouldDelegateToClient() throws Exception {
        when(userClient.getById(1L)).thenReturn(ok(new UserDto(1L, "user", "user@mail.com")));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllShouldDelegateToClient() throws Exception {
        when(userClient.getAll()).thenReturn(ok(List.of(new UserDto(1L, "user", "user@mail.com"))));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void deleteShouldDelegateToClient() throws Exception {
        when(userClient.delete(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userClient).delete(1L);
    }

    private ResponseEntity<Object> ok(Object body) {
        return ResponseEntity.ok(body);
    }

}
