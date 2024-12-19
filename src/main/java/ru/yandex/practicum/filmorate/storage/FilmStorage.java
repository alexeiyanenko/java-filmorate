package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    //Методы для фильмов

    Optional<Film> addFilm(Film film);

    Optional<Film> updateFilm(Film film);

    Optional<Film> getFilmById(Long filmId);

    List<Film> getAllFilms();

    boolean deleteFilmById(Long filmId);

    List<Film> findFilmsBySubstring(String query, String by);

    List<Film> getCommonFilms(Long userId, Long friendId);

    boolean isFilmExist(Long filmId);

    // Методы для жанров

    Film updateGenres(Film film);

    Optional<Genre> getGenreById(Long genreId);

    Set<Genre> getGenresByFilmId(Long filmId);

    Map<Long, Set<Genre>> getGenresForAllFilms();

    List<Genre> getAllGenres();

    void deleteGenresByFilmId(Long filmId);

    boolean isGenreExist(Long genreId);

    // Методы для MPA

    Optional<MPA> getMPAById(Long mpaId);

    List<MPA> getAllMPAs();
}
