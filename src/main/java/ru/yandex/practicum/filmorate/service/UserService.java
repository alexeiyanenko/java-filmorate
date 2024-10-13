package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final InMemoryUserStorage userStorage;

    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void friend(int id1, int id2) {
        User user = userStorage.getById(id1);
        User friend = userStorage.getById(id2);
        user.addFriend(id2);
        friend.addFriend(id1);
    }

    public void unfriend(int id1, int id2) {
        User user = userStorage.getById(id1);
        User friend = userStorage.getById(id2);
        user.removeFriend(id2);
        friend.removeFriend(id1);
    }

    public List<User> getCommonFriends(int id1, int id2) {
        User user = userStorage.getById(id1);
        User user2 = userStorage.getById(id2);
        Set<Integer> commonFriendsIds = user.getFriends().stream()
                .filter(user2.getFriends()::contains)
                .collect(Collectors.toSet());

        return commonFriendsIds.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }
}
