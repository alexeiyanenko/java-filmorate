package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Long userId);

    Optional<User> getUserById(Long userId);

    Set<User> getFriends(Long userId);

    List<User> getAllUsers();

    boolean isUserExist(Long userId);
}
