package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {
    Film updateGenres(Film film);

    Optional<Genre> getGenreById(Long genreId);

    Set<Genre> getGenresByFilmId(Long filmId);

    Map<Long, Set<Genre>> getGenresForAllFilms();

    List<Genre> getAllGenres();

    void deleteGenresByFilmId(Long filmId);

    boolean isGenreExist(Long genreId);
}
