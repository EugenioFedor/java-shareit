package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto updateUser(long id, UserDto userDto);

    UserDto getUserById(long id);

    List<UserDto> getAllUsers();

    void deleteUser(long id);
}