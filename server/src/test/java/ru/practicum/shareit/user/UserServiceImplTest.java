package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createShouldSaveUser() {
        UserDto dto = new UserDto(null, "User", "user@mail.com");
        User user = new User(null, "User", "user@mail.com", null);
        User saved = new User(1L, "User", "user@mail.com", null);
        UserDto expected = new UserDto(1L, "User", "user@mail.com");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userMapper.toUser(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(saved);
        when(userMapper.toUserDto(saved)).thenReturn(expected);

        UserDto result = userService.createUser(dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).save(user);
    }

    @Test
    void createShouldThrowWhenEmailExists() {
        UserDto dto = new UserDto(null, "User", "user@mail.com");

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateShouldPatchOnlyNotNullFields() {
        User user = new User(1L, "Old", "old@mail.com", null);
        UserDto patch = new UserDto(null, "New", null);
        User saved = new User(1L, "New", "old@mail.com", null);
        UserDto expected = new UserDto(1L, "New", "old@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(saved);
        when(userMapper.toUserDto(saved)).thenReturn(expected);

        UserDto result = userService.updateUser(1L, patch);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(user.getEmail()).isEqualTo("old@mail.com");
    }

    @Test
    void getShouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteShouldCallRepository() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}