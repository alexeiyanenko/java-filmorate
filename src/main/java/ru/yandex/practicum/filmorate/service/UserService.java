package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

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

    public void friend(int id1, int id2) {
        User user = userStorage.getById(id1)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id1 + " не найден."));
        User friend = userStorage.getById(id2)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id2 + " не найден."));
        user.addFriend(id2);
        friend.addFriend(id1);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Пользователи с id {} и {} теперь друзья.", id1, id2);
    }

    public void unfriend(int id1, int id2) {
        User user = userStorage.getById(id1)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id1 + " не найден."));
        User friend = userStorage.getById(id2)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id2 + " не найден."));
        user.removeFriend(id2);
        friend.removeFriend(id1);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public List<User> getCommonFriends(int id1, int id2) {
        User user = userStorage.getById(id1)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id1 + " не найден."));
        User user2 = userStorage.getById(id2)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id2 + " не найден."));
        Set<Integer> commonFriendsIds = user.getFriendsIDs().stream()
                .filter(user2.getFriendsIDs()::contains)
                .collect(Collectors.toSet());

        return commonFriendsIds.stream()
                .map(userStorage::getById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
