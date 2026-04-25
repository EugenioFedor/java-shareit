package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto getUserById(long userId) {
        return UserMapper.toUserDto(getUserOrThrow(userId));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        validateEmail(userDto.getEmail());
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Email already exists");
        }
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto updateUser(long userId, UserDto userDto) {
        User user = getUserOrThrow(userId);

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());
            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
                throw new ConflictException("Email already exists");
            }
            user.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(userRepository.update(user));
    }

    @Override
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Invalid email");
        }
    }
}
