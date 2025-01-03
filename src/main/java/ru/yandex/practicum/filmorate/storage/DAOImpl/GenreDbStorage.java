package ru.yandex.practicum.filmorate.storage.DAOImpl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Primary
@Repository
@Slf4j
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film updateGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sqlQuery = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                if (!isGenreExist(genre.getId())) {
                    throw new ValidationException("Некорректный ID жанра: " + genre.getId());
                }
                jdbcTemplate.update(sqlQuery, film.getId(), genre.getId());
            }

            film.setGenres(getGenresByFilmId(film.getId()));
            return film;
        }

        film.setGenres(getGenresByFilmId(film.getId()));
        return film;
    }

    @Override
    public Optional<Genre> getGenreById(@NonNull Long genreId) {
        String sqlQuery = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::mapRowToGenre, genreId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Set<Genre> getGenresByFilmId(Long filmId) {
        String sqlQuery = "SELECT g.genre_id, g.genre_name " +
                "FROM genres g " +
                "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? ";

        return new LinkedHashSet<>(jdbcTemplate.query(sqlQuery, (rs, rowNum) ->
                new Genre(rs.getLong("genre_id"), rs.getString("genre_name")), filmId));
    }

    public Map<Long, Set<Genre>> getGenresForAllFilms() {
        String sqlQuery = "SELECT fg.film_id, g.genre_id, g.genre_name " +
                "FROM film_genre fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id";

        return jdbcTemplate.query(sqlQuery, rs -> {
            Map<Long, Set<Genre>> genresMap = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Genre genre = new Genre(rs.getLong("genre_id"), rs.getString("genre_name"));

                genresMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
            }
            return genresMap;
        });
    }

    @Override
    public List<Genre> getAllGenres() {
        String sqlQuery = "SELECT * FROM genres";
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    @Override
    public void deleteGenresByFilmId(Long filmId) {
        String sqlQuery = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    @Override
    public boolean isGenreExist(Long genreId) {
        String sqlQuery = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, Boolean.class, genreId);
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getLong("genre_id"), rs.getString("genre_name"));
    }
}
