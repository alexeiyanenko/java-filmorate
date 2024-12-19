package ru.yandex.practicum.filmorate.storage.memoryImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private Long currentId = 1L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        user.setId(currentId++);
        users.put(user.getId(), user);
        log.info("Пользователь создан: {}", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с ID {} не найден для обновления.", user.getId());
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден.");
        }
        users.put(user.getId(), user);
        log.info("Пользователь обновлён: {}", user);
        return user;
    }

    @Override
    public void deleteUser(Long userId) {

    }

    @Override
    public Optional<User> getUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null.");
        }

        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Set<User> getFriends(Long userId) {
        return Set.of();
    }

    public Set<User> getFriends(long id) {
        return getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден."))
                .getFriends();
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean isUserExist(Long userId) {
        return users.containsKey(userId);
    }
}
