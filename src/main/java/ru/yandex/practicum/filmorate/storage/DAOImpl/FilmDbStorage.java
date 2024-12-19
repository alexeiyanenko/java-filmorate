package ru.yandex.practicum.filmorate.storage.DAOImpl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validation.ValidationException;

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
        film.setMpa(
                film.getMpa() != null
                        ? getMPAById(film.getMpa().getId()).orElseThrow()
                        : null
        );

        return Optional.of(updateGenres(film));
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
                film.getMpa().getId(),
                film.getId());

        if (rowsUpdated == 0) {
            throw new NoSuchElementException("Фильм " + film.getId() + " не найден.");
        }

        film.setMpa(getMPAById(film.getMpa().getId()).orElseThrow());
        deleteGenresByFilmId(film.getId());
        return Optional.of(updateGenres(film));
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

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sqlQuery = "SELECT f.* FROM films f " +
                "JOIN likes l1 ON f.film_id = l1.film_id AND l1.user_id = ? " +
                "JOIN likes l2 ON f.film_id = l2.film_id AND l2.user_id = ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, userId, friendId);
    }

    @Override
    public boolean isFilmExist(Long filmId) {
        String sqlQuery = "SELECT EXISTS (SELECT 1 FROM films WHERE film_id = ?)";
        return jdbcTemplate.queryForObject(sqlQuery, Boolean.class, filmId);
    }

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

    @Override
    public Optional<MPA> getMPAById(Long mpaId) {
        String sqlQuery = "SELECT * FROM mpa WHERE mpa_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::mapRowToMPARating, mpaId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<MPA> getAllMPAs() {
        String sqlQuery = "SELECT * FROM mpa";
        return jdbcTemplate.query(sqlQuery, this::mapRowToMPARating);
    }

    //mapRowTo...
    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("film_name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));

        // Проверяем наличие MPA рейтинга
        film.setMpa(getMPAById(rs.getLong("mpa_id"))
                .orElse(new MPA(0L, "Unknown", "No description available")));

        // Получение жанров
        Set<Genre> genres = getGenresByFilmId(film.getId());
        film.setGenres(genres.isEmpty() ? new HashSet<>() : genres);

        return film;
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getLong("genre_id"), rs.getString("genre_name"));
    }

    private MPA mapRowToMPARating(ResultSet rs, int rowNum) throws SQLException {
        return new MPA(rs.getLong("mpa_id"), rs.getString("mpa_name"), rs.getString("description"));
    }
}
