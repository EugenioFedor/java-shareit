package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public User save(User user) {
        user.setId(currentId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteById(long userId) {
        users.remove(userId);
    }

    @Override
    public boolean existsById(long userId) {
        return users.containsKey(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail() != null && user.getEmail().equals(email));
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, long userId) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail() != null
                        && user.getEmail().equals(email)
                        && !user.getId().equals(userId));
    }
}
