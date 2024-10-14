package ru.yandex.practicum.filmorate.storage.film;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addFilm(@Valid @RequestBody Film film);

    Film updateFilm(@Valid @RequestBody Film updatedFilm);

    Film getById(Integer id);

    List<Film> getAllFilms();
}
