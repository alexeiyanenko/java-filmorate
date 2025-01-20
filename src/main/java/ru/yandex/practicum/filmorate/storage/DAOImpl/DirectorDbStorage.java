package ru.yandex.practicum.filmorate.storage.DAOImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;

@Primary
@Repository
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getAllDirectors() {
        String sqlQuery = "SELECT * FROM directors";
        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector);
    }

    @Override
    public Director getDirectorById(Long id) {
        String sqlQuery = "SELECT * FROM directors WHERE director_id = ?";
        if (!isDirectorExist(id)) {
            throw new NotFoundException("Режиссер с таким ID не найден " + id);
        }

        Optional<Director> directorOptional = Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery,
                this::mapRowToDirector, id));
        if (directorOptional.isPresent()) {
            log.info("Получили режиссера с id: {}", id);
            return directorOptional.get();
        }
        log.error("Режиссер с таким ID {} не найден", id);
        throw new NotFoundException("Режиссер с таким ID не найден");
    }

    @Override
    public boolean isDirectorExist(Long id) {
        String sqlQuery = "SELECT EXISTS (SELECT 1 FROM directors WHERE director_id = ?)";
        return jdbcTemplate.queryForObject(sqlQuery, Boolean.class, id);
    }

    @Override
    public Director addDirector(Director director) {
        String sqlQuery = "INSERT INTO directors (director_name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = "UPDATE directors SET director_name = ? WHERE director_id = ?";

        int rowsUpdated = jdbcTemplate.update(sqlQuery,
                director.getName(),
                director.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Директор с ID " + director.getId() + " не найден.");
        }
        return director;
    }

    @Override
    public void deleteDirectorById(Long id) {
        String sqlQuery = "DELETE FROM directors WHERE director_id = ?";
        int rowsDeleted = jdbcTemplate.update(sqlQuery, id);

        if (rowsDeleted == 0) {
            throw new NoSuchElementException("Режиссер с ID " + id + " не найден.");
        }
    }

    @Override
    public Set<Director> getDirectorsByFilmId(Long id) {
        String sqlQuery = "SELECT d.director_id, d.director_name " +
                "FROM directors AS d " +
                "JOIN film_director fd ON d.director_id = fd.director_id " +
                "WHERE fd.film_id = ?";

        return new LinkedHashSet<>(jdbcTemplate.query(sqlQuery, (rs, rowNum) ->
                new Director(rs.getLong("director_id"), rs.getString("director_name")), id));
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        Director director = new Director();
        director.setId(rs.getLong("director_id"));
        director.setName(rs.getString("director_name"));
        return director;
    }

    @Override
    public Film addDirectorToFilm(Film film) {
        String sqlQuery = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";

        for (Director director : film.getDirectors()) {
            if (!isDirectorExist(director.getId())) {
                throw new ValidationException("Некорректный ID режиссера: " + director.getId());
            }
            jdbcTemplate.update(sqlQuery, film.getId(), director.getId());
        }
        film.setDirectors(getDirectorsByFilmId(film.getId()));
        return film;
    }

    @Override
    public Film updateDirectorToFilm(Film film) {
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ?", film.getId());

        String sqlQuery = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";

        for (Director director : film.getDirectors()) {
            if (!isDirectorExist(director.getId())) {
                throw new ValidationException("Некорректный ID режиссера: " + director.getId());
            }
            jdbcTemplate.update(sqlQuery, film.getId(), director.getId());
        }
        film.setDirectors(getDirectorsByFilmId(film.getId()));
        return film;
    }
}
