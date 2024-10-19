package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        setDefaultNameIfEmpty(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User updatedUser) {
        setDefaultNameIfEmpty(updatedUser);
        return userStorage.updateUser(updatedUser);
    }

    private void setDefaultNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public User getById(int id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден."));
    }

    public Set<Integer> getFriendsIDs(int id) {
        return userStorage.getFriendsIDs(id);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void friend(int id, int friendId) {
        User user = userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден."));
        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден."));
        user.addFriend(friendId);
        friend.addFriend(id);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Пользователи с id {} и {} теперь друзья.", id, friendId);
    }

    public void unfriend(int id, int friendId) {
        User user = userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден."));
        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден."));
        user.removeFriend(friendId);
        friend.removeFriend(id);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        User user = userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден."));
        User user2 = userStorage.getById(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + otherId + " не найден."));
        Set<Integer> commonFriendsIds = user.getFriendsIDs().keySet().stream()
                .filter(user2.getFriendsIDs().keySet()::contains)
                .collect(Collectors.toSet());

        return commonFriendsIds.stream()
                .map(friendId -> userStorage.getById(friendId)
                        .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден.")))
                .collect(Collectors.toList());
    }
}
