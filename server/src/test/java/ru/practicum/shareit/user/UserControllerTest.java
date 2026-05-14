package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createShouldReturnUser() throws Exception {
        UserDto request = new UserDto(null, "user", "user@mail.com");
        UserDto response = new UserDto(1L, "user", "user@mail.com");

        when(userService.createUser(any(UserDto.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@mail.com"));
    }

    @Test
    void updateShouldDelegateToService() throws Exception {
        UserDto request = new UserDto(null, "new", null);
        UserDto response = new UserDto(1L, "new", "old@mail.com");

        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(response);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new"));

        verify(userService).updateUser(eq(1L), any(UserDto.class));
    }


    @Test
    void getByIdShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(new UserDto(1L, "user", "user@mail.com"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@mail.com"));
    }

    @Test
    void getAllShouldReturnUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(new UserDto(1L, "user", "user@mail.com")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void deleteShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L);
    }
}
