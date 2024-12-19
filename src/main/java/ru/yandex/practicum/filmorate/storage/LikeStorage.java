package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LikeStorage {

    boolean like(Long filmId, Long userId);

    boolean unlike(Long filmId, Long userId);

    List<Long> getLikesByFilmId(Long filmId);

    Map<Long, Set<Long>> getAllLikes();
}
