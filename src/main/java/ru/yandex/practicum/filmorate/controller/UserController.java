package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EventService eventService;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.debug("Создание пользователя: {}", user);
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.debug("Обновление пользователя: {}", user);
        return userService.updateUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        log.debug("Удалён пользователь с id: {}", id);
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Запрос на получение всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        log.debug("Запрос пользователя с id: {}", id);
        return userService.getUserById(id);
    }

    @GetMapping("{id}/feed")
    public List<Event> getAllEventsById(@PathVariable long id) {
        log.debug("Запрос ленты событий от пользователя с id: {}", id);
        return eventService.getAllEventsById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void friend(@PathVariable long id, @PathVariable long friendId) {
        userService.friend(id, friendId);
        log.debug("Добавление в друзья: {} -> {}", id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void unfriend(@PathVariable long id, @PathVariable long friendId) {
        userService.unfriend(id, friendId);
        log.debug("Удаление из друзей: {} -> {}", id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable long id) {
        log.debug("Получение друзей пользователя с id: {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        log.debug("Получение общих друзей между {} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable long id) {
        log.debug("Получение рекомендаций для {}", id);
        return userService.getRecommendations(id);
    }
}