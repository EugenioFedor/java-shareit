package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    private UserMapper userMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        userService = new UserServiceImpl(userMapper, userRepository);
    }

    @Test
    void shouldCreateUser() {
        UserDto dto = new UserDto(null, "User", "user@mail.com");
        User saved = new User(1L, "User", "user@mail.com", null);

        when(userRepository.existsByEmail("user@mail.com")).thenReturn(false);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(saved);

        UserDto result = userService.createUser(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("User");
        assertThat(result.getEmail()).isEqualTo("user@mail.com");
    }

    @Test
    void shouldThrowWhenCreateWithInvalidEmail() {
        UserDto dto = new UserDto(null, "User", "wrong-email");

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenCreateWithDuplicateEmail() {
        UserDto dto = new UserDto(null, "User", "user@mail.com");

        when(userRepository.existsByEmail("user@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldUpdateNameAndEmail() {
        User user = new User(1L, "Old", "old@mail.com", null);
        UserDto update = new UserDto(null, "New", "new@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("new@mail.com", 1L)).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        UserDto result = userService.updateUser(1L, update);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("new@mail.com");
    }

    @Test
    void shouldUpdateOnlyName() {
        User user = new User(1L, "Old", "old@mail.com", null);
        UserDto update = new UserDto(null, "New", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDto result = userService.updateUser(1L, update);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getEmail()).isEqualTo("old@mail.com");
    }

    @Test
    void shouldThrowWhenUpdateMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, new UserDto(null, "User", null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenUpdateDuplicateEmail() {
        User user = new User(1L, "User", "old@mail.com", null);
        UserDto update = new UserDto(null, null, "new@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("new@mail.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, update))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldGetUserById() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(new User(1L, "User", "user@mail.com", null)));

        UserDto result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenGetMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetAllUsers() {
        when(userRepository.findAll())
                .thenReturn(List.of(new User(1L, "User", "user@mail.com", null)));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldDeleteUser() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}