package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody @Valid User updatedUser) {
        return ResponseEntity.ok(userService.updateUser(updatedUser));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable int id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> friend(@PathVariable int id, @PathVariable int friendId) {
        userService.friend(id, friendId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> unfriend(@PathVariable int id, @PathVariable int friendId) {
        userService.unfriend(id, friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<Map<String, Integer>>> getFriendsIDs(@PathVariable int id) {
        Set<Integer> friendsIDs = userService.getFriendsIDs(id);

        List<Map<String, Integer>> friends = friendsIDs.stream()
                .map(friendId -> Map.of("id", friendId))
                .toList();
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        return ResponseEntity.ok(userService.getCommonFriends(id, otherId));
    }
}