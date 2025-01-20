package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    //Методы для фильмов

    Optional<Film> addFilm(Film film);

    Optional<Film> updateFilm(Film film);

    Optional<Film> getFilmById(Long filmId);

    List<Film> getAllFilms();

    boolean deleteFilmById(Long filmId);

    List<Film> findFilmsBySubstring(String query, String by);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getDirectorFilms(Long directorId, String sortBy);

    boolean isFilmExist(Long filmId);
}
