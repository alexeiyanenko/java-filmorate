package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User updatedUser) {
        return userStorage.updateUser(updatedUser);
    }

    public User getById(int id) {
        return userStorage.getById(id);
    }

    public Set<Integer> getFriendsIDs(int id) {
        return userStorage.getFriendsIDs(id);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void friend(int id1, int id2) {
        User user = userStorage.getById(id1);
        User friend = userStorage.getById(id2);
        user.addFriend(id2);
        friend.addFriend(id1);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Пользователи с id {} и {} теперь друзья.", id1, id2);
    }

    public void unfriend(int id1, int id2) {
        User user = userStorage.getById(id1);
        User friend = userStorage.getById(id2);
        user.removeFriend(id2);
        friend.removeFriend(id1);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public List<User> getCommonFriends(int id1, int id2) {
        User user = userStorage.getById(id1);
        User user2 = userStorage.getById(id2);
        Set<Integer> commonFriendsIds = user.getFriendsIDs().stream()
                .filter(user2.getFriendsIDs()::contains)
                .collect(Collectors.toSet());

        return commonFriendsIds.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }
}
