package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User updatedUser);

    Optional<User> getById(Integer id);

    Set<Integer> getFriendsIDs(int id);

    List<User> getAllUsers();

    boolean isUserExist(int id);
}
