package ru.yandex.practicum.filmorate.storage.DAOImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Primary
@Repository
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Film> addFilm(Film film) {
        String sqlQuery = "INSERT INTO films (film_name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setLong(4, film.getDuration());
            stmt.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return Optional.of(film);
    }

    @Override
    public Optional<Film> updateFilm(Film film) {
        String sqlQuery = "UPDATE films SET film_name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? WHERE film_id = ?";
        int rowsUpdated = jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        if (rowsUpdated == 0) {
            throw new NoSuchElementException("Фильм с ID " + film.getId() + " не найден.");
        }

        return Optional.of(film);
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        String sqlQuery = "SELECT * FROM films WHERE film_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, filmId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAllFilms() {
        String sqlQuery = "SELECT * FROM films";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public boolean deleteFilmById(Long filmId) {
        String sqlQuery = "DELETE FROM films WHERE film_id = ?";
        return jdbcTemplate.update(sqlQuery, filmId) > 0;
    }

    @Override
    public List<Film> findFilmsBySubstring(String query, String by) {
        String sqlQuery;
        if ("name".equalsIgnoreCase(by)) {
            sqlQuery = "SELECT * FROM films WHERE LOWER(film_name) LIKE LOWER(?)";
        } else if ("description".equalsIgnoreCase(by)) {
            sqlQuery = "SELECT * FROM films WHERE LOWER(description) LIKE LOWER(?)";
        } else {
            throw new IllegalArgumentException("Параметр 'by' должен быть 'name' или 'description'");
        }
        String searchQuery = "%" + query + "%";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, searchQuery);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sqlQuery = "SELECT f.*, COUNT(l.film_id) AS likes_count " +
                "FROM films f " +
                "JOIN likes l1 ON f.film_id = l1.film_id AND l1.user_id = ? " +
                "JOIN likes l2 ON f.film_id = l2.film_id AND l2.user_id = ? " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY likes_count DESC";

        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, userId, friendId);
    }

    @Override
    public boolean isFilmExist(Long filmId) {
        String sqlQuery = "SELECT EXISTS (SELECT 1 FROM films WHERE film_id = ?)";
        return jdbcTemplate.queryForObject(sqlQuery, Boolean.class, filmId);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("film_name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));

        // Установка MPA
        if (rs.getObject("mpa_id") != null) {
            film.setMpa(new MPA(rs.getLong("mpa_id"), null, null));
        }

        return film;
    }
}
