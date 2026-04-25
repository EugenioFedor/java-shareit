package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();

    Optional<User> findById(long userId);

    User save(User user);

    User update(User user);

    void deleteById(long userId);

    boolean existsById(long userId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, long userId);
}
