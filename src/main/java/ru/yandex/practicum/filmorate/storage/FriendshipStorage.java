package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendshipStorage {

    boolean addFriendship(User friendRequest, User friendResponse);

    boolean deleteFriendship(User friendRequest, User friendResponse);

    List<Long> getFriendships(Long userId);

}
