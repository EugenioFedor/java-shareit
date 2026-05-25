package ru.practicum.shareit.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void shouldMapUserToDto() {
        User user = new User(1L, "User", "user@mail.com", null);

        UserDto dto = mapper.toUserDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("User");
        assertThat(dto.getEmail()).isEqualTo("user@mail.com");
    }

    @Test
    void shouldMapDtoToUser() {
        UserDto dto = new UserDto(1L, "User", "user@mail.com");

        User user = mapper.toUser(dto);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("User");
        assertThat(user.getEmail()).isEqualTo("user@mail.com");
    }
}