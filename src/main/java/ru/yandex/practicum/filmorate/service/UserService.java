package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    @Qualifier("friendshipDbStorage")
    private final FriendshipStorage friendshipStorage;
    @Qualifier("eventDbStorage")
    private final EventStorage eventStorage;
    @Qualifier("likeDbStorage")
    private final LikeStorage likeStorage;
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

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

    public void deleteUser(long id) {
        userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден."));
        userStorage.deleteUser(id);
        log.info("Удалён пользователь с ID {}", id);
    }

    public User getUserById(long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден."));
    }

    public List<User> getFriends(long id) {
        if (!userStorage.isUserExist(id)) {
            log.warn("Пользователь с ID {} не найден.", id);
            throw new NotFoundException("Пользователь с ID " + id + " не найден.");
        }

        List<Long> friendIds = friendshipStorage.getFriendships(id);
        log.debug("Найдены ID друзей пользователя {}: {}", id, friendIds);

        return friendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void friend(long userId, long friendId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден."));

        try {
            boolean isAdded = friendshipStorage.addFriendship(user, friend);
            if (!isAdded) {
                log.info("Дружба уже существует между {} и {}.", userId, friendId);
            } else {
                eventStorage.createEvent(userId, Event.EventType.FRIEND, Event.Operation.ADD, friendId);
                log.info("Добавлена дружба между {} и {}.", userId, friendId);
            }
        } catch (RuntimeException e) {
            log.error("Ошибка при добавлении дружбы: {}", e.getMessage());
            throw new RuntimeException("Ошибка сервера при добавлении дружбы.", e);
        }
    }

    public void unfriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден."));

        friendshipStorage.deleteFriendship(user, friend);
        eventStorage.createEvent(userId, Event.EventType.FRIEND, Event.Operation.REMOVE, friendId);
    }

    public List<User> getCommonFriends(long id, long otherId) {
        if (!userStorage.isUserExist(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден.");
        }
        if (!userStorage.isUserExist(otherId)) {
            throw new NotFoundException("Пользователь с ID " + otherId + " не найден.");
        }

        List<Long> userFriends = friendshipStorage.getFriendships(id);
        List<Long> otherUserFriends = friendshipStorage.getFriendships(otherId);

        Set<Long> commonFriendIds = userFriends.stream()
                .filter(otherUserFriends::contains)
                .collect(Collectors.toSet());

        return commonFriendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<Film> getRecommendations(long id) {
        List<Long> userLikes = likeStorage.getLikesByUserId(id);
        List<User> users = userStorage.getAllUsers();

        // Находим лайки всех пользователей
        Map<Long, List<Long>> usersLikesMap = new HashMap<>();
        for (User user : users) {
            if (!user.getId().equals(id)) {
                List<Long> otherUserLikes = likeStorage.getLikesByUserId(user.getId());
                usersLikesMap.put(user.getId(), otherUserLikes);
            }
        }

        // Находим пользователя с максимальным пересечением лайков
        Long similarUserId = usersLikesMap.entrySet().stream()
                .max(Comparator.comparingInt(entry -> getIntersectionCount(userLikes, entry.getValue())))
                .map(Map.Entry::getKey)
                .orElse(null);

        // Рекомендуем фильмы если похожий пользователь найден
        if (similarUserId != null) {
            List<Long> similarUserLikes = likeStorage.getLikesByUserId(similarUserId);
            return similarUserLikes.stream()
                    .filter(filmId -> !userLikes.contains(filmId))
                    .map(filmStorage::getFilmById)
                    .flatMap(Optional::stream)
                    .toList();
        }

        // Если похожих пользователей нет, возвращаем пустой список
        return Collections.emptyList();

    }

    // Метод для нахождения пересечений
    private int getIntersectionCount(List<Long> list1, List<Long> list2) {
        Set<Long> set1 = new HashSet<>(list1);
        set1.retainAll(list2);
        return set1.size();
    }
}
