package ru.yandex.practicum.filmorate.storage.film;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private int currentId = 1;
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public void addFilm(@Valid @RequestBody Film film) {
        film.setId(currentId++);
        films.put(film.getId(), film);
        log.info("Фильм добавлен: {}", film);
    }

    @Override
    public void updateFilm(@Valid @RequestBody Film updatedFilm) {
        if (!films.containsKey(updatedFilm.getId())) {
            log.warn("Фильм с ID {} не найден для обновления.", updatedFilm.getId());
            throw new IllegalArgumentException("Фильм с ID " + updatedFilm.getId() + " не найден.");
        }
        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Фильм обновлён: {}", updatedFilm);
    }

    public Film getById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null.");
        }

        Film film = films.get(id);
        if (film == null) {
            throw new IllegalArgumentException("Фильм с ID " + id + " не найден.");
        }

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return List.copyOf(films.values());
    }
}
