package ru.yandex.practicum.filmorate.storage.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private int currentId = 1;
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User createUser(@RequestBody @Valid User user) {
        setDefaultNameIfEmpty(user);
        user.setId(currentId++);
        users.put(user.getId(), user);
        log.info("Пользователь создан: {}", user);
        return user;
    }

    @Override
    public User updateUser(@RequestBody @Valid User updatedUser) {
        if (!users.containsKey(updatedUser.getId())) {
            log.warn("Пользователь с ID {} не найден для обновления.", updatedUser.getId());
            throw new NotFoundException("Пользователь с ID " + updatedUser.getId() + " не найден.");
        }
        setDefaultNameIfEmpty(updatedUser);
        users.put(updatedUser.getId(), updatedUser);
        log.info("Пользователь обновлён: {}", updatedUser);
        return updatedUser;
    }

    private void setDefaultNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    @Override
    public User getById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null.");
        }

        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден.");
        }

        return user;
    }

    public Set<Integer> getFriendsIDs(int id) {
        User user = this.getById(id);
        return user.getFriendsIDs();
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
