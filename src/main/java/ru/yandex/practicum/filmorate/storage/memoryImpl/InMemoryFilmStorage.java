package ru.yandex.practicum.filmorate.storage.memoryImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {

    private Long currentId = 1L;
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Optional<Film> addFilm(Film film) {
        film.setId(currentId++);
        films.put(film.getId(), film);
        log.info("Фильм добавлен: {}", film);
        return Optional.of(film);
    }

    @Override
    public Optional<Film> updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с ID {} не найден для обновления.", film.getId());
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден.");
        }
        films.put(film.getId(), film);
        log.info("Фильм обновлён: {}", film);
        return Optional.of(film);
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        if (filmId == null) {
            throw new IllegalArgumentException("ID не может быть null.");
        }

        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public boolean deleteFilmById(Long filmId) {
        if (films.containsKey(filmId)) {
            films.remove(filmId);
            log.info("Фильм с ID {} удалён.", filmId);
            return true;
        } else {
            log.warn("Фильм с ID {} не найден для удаления.", filmId);
            return false;
        }
    }

    @Override
    public List<Film> getAllFilms() {
        return List.copyOf(films.values());
    }

    @Override
    public boolean isFilmExist(Long filmId) {
        boolean exists = films.containsKey(filmId);
        log.info("Фильм с ID {} {}.", filmId, exists ? "существует" : "не существует");
        return exists;
    }

    @Override
    public Film updateGenres(Film film) {
        return null;
    }

    @Override
    public List<Film> findFilmsBySubstring(String query, String by) {
        return List.of();
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return List.of();
    }

    @Override
    public Optional<Genre> getGenreById(Long genreId) {
        return Optional.empty();
    }

    @Override
    public List<Genre> getAllGenres() {
        return List.of();
    }

    @Override
    public void deleteGenresByFilmId(Long filmId) {

    }

    @Override
    public boolean isGenreExist(Long genreId) {
        return false;
    }

    @Override
    public Set<Genre> getGenresByFilmId(Long filmId) {
        return Set.of();
    }

    @Override
    public Map<Long, Set<Genre>> getGenresForAllFilms() {
        return Map.of();
    }

    @Override
    public Optional<MPA> getMPAById(Long mpaId) {
        return Optional.empty();
    }

    @Override
    public List<MPA> getAllMPAs() {
        return List.of();
    }
}