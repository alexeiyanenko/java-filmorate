package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@Controller
@RequestMapping("/users")
public class UserController {

    private int currentId = 1;
    private final Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
        setDefaultNameIfEmpty(user);
        user.setId(currentId++);
        users.put(user.getId(), user);
        log.info("Пользователь создан: {}", user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping
    public ResponseEntity<Object> updateUser(@RequestBody @Valid User updatedUser) {
        setDefaultNameIfEmpty(updatedUser);
        if (users.containsKey(updatedUser.getId())) {
            users.put(updatedUser.getId(), updatedUser);
            log.info("Пользователь обновлён: {}", updatedUser);
            return ResponseEntity.ok(updatedUser);
        } else {
            log.warn("Пользователь с ID {} не найден для обновления.", updatedUser.getId());
            return new ResponseEntity<>(new ErrorResponse("Пользователь не найден"), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> userList = users.values().stream().collect(Collectors.toList());
        return ResponseEntity.ok(userList);
    }

    private void setDefaultNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}