package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface DirectorStorage {

    List<Director> getAllDirectors();

    Director getDirectorById(Long id);

    boolean isDirectorExist(Long id);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirectorById(Long id);

    Set<Director> getDirectorsByFilmId(Long id);

    Film addDirectorToFilm(Film film);

    Film updateDirectorToFilm(Film film);
}
