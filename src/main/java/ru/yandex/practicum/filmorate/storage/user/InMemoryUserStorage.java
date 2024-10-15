package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private int currentId = 1;
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        user.setId(currentId++);
        users.put(user.getId(), user);
        log.info("Пользователь создан: {}", user);
        return user;
    }

    @Override
    public User updateUser(User updatedUser) {
        if (!users.containsKey(updatedUser.getId())) {
            log.warn("Пользователь с ID {} не найден для обновления.", updatedUser.getId());
            throw new NotFoundException("Пользователь с ID " + updatedUser.getId() + " не найден.");
        }
        users.put(updatedUser.getId(), updatedUser);
        log.info("Пользователь обновлён: {}", updatedUser);
        return updatedUser;
    }

    @Override
    public Optional<User> getById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null.");
        }

        return Optional.ofNullable(users.get(id));
    }

    public Set<Integer> getFriendsIDs(int id) {
        return getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден."))
                .getFriendsIDs();
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean isUserExist(int id) {
        return users.containsKey(id);
    }
}
