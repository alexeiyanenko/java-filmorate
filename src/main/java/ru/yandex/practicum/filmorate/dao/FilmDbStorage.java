package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";

        Integer ratingId = (film.getRating() != null) ? film.getRating().ordinal() + 1 : null;

        jdbcTemplate.update(sql, film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration(), ratingId);

        log.info("Фильм добавлен: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film updatedFilm) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";

        Integer ratingId = (updatedFilm.getRating() != null) ? updatedFilm.getRating().ordinal() + 1 : null; // Set to null if no rating

        if (jdbcTemplate.update(sql, updatedFilm.getName(), updatedFilm.getDescription(),
                updatedFilm.getReleaseDate(), updatedFilm.getDuration(), ratingId,
                updatedFilm.getId()) == 0) {
            throw new NotFoundException("Фильм с ID " + updatedFilm.getId() + " не найден.");
        }

        log.info("Фильм обновлён: {}", updatedFilm);
        return updatedFilm;
    }

    @Override
    public Optional<Film> getById(Integer id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToFilm, id).stream().findFirst();
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        // Map rating_id to the MPA enum
        int ratingId = rs.getInt("rating_id");
        if (ratingId != 0) {
            film.setRating(Film.MPA.values()[ratingId - 1]);
        } else {
            film.setRating(null);
        }

        return film;
    }

}
