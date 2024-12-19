package ru.yandex.practicum.filmorate.storage.DAOImpl;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Primary
@Repository
@Slf4j
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {

    private final JdbcTemplate jdbcTemplate;

    public boolean like(Long filmId, Long userId) {
        String sqlQuery = "merge into likes (film_id, user_id) " +
                " values (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        return true;
    }

    public boolean unlike(Long filmId, Long userId) {
        String sqlQuery = "delete from likes where film_id = ? and user_id = ? ";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        return true;
    }

    public List<Long> getLikesByFilmId(Long filmId) {
        String sqlQuery = "select user_id from likes where film_id = ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> rs.getLong("user_id"), filmId);
    }

    public Map<Long, Set<Long>> getAllLikes() {
        String sqlQuery = "SELECT film_id, user_id FROM likes";
        return jdbcTemplate.query(sqlQuery, rs -> {
            Map<Long, Set<Long>> allLikes = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Long userId = rs.getLong("user_id");
                allLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
            }
            return allLikes;
        });
    }
}
