package ru.yandex.practicum.filmorate.storage.user;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserStorage {

    User createUser(@RequestBody @Valid User user);

    User updateUser(@RequestBody @Valid User updatedUser);

    User getById(Integer id);

    Set<Integer> getFriendsIDs(int id);

    List<User> getAllUsers();
}
