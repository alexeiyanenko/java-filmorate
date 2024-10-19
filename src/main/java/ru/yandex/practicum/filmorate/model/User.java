package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class User {
    private int id;

    @Email(message = "Электронная почта должна содержать символ '@'.")
    @NotBlank(message = "Электронная почта не может быть пустой.")
    private String email;

    @NotBlank(message = "Логин не может быть пустым и содержать пробелы.")
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелов.")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения не может быть пустой.")
    @PastOrPresent(message = "Дата рождения не может быть в будущем.")
    private LocalDate birthday;

    private final Map<Integer, FriendshipStatus> friendsIDs = new HashMap<>();

    public void addFriend(int friendId) {
        friendsIDs.put(friendId, FriendshipStatus.UNCONFIRMED);
    }

    public void confirmFriendship(int friendId) {
        if (friendsIDs.containsKey(friendId)) {
            friendsIDs.put(friendId, FriendshipStatus.CONFIRMED);
        }
    }

    public enum FriendshipStatus {
        UNCONFIRMED,
        CONFIRMED
    }

    public void removeFriend(int id) {
        friendsIDs.remove(id);
    }
}

