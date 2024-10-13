package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final InMemoryFilmStorage filmStorage;

    public FilmService(InMemoryFilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void like(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        film.addLike(userId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        film.removeLike(userId);
    }

    public List<Film> getMostPopularFilms(int limit) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
