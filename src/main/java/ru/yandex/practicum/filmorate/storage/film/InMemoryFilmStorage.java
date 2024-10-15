package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {

    private int currentId = 1;
    private final Map<Integer, Film> films = new HashMap<>();
    private final UserStorage userStorage;

    @Override
    public Film addFilm(Film film) {
        film.setId(currentId++);
        films.put(film.getId(), film);
        log.info("Фильм добавлен: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film updatedFilm) {
        if (!films.containsKey(updatedFilm.getId())) {
            log.warn("Фильм с ID {} не найден для обновления.", updatedFilm.getId());
            throw new NotFoundException("Фильм с ID " + updatedFilm.getId() + " не найден.");
        }
        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Фильм обновлён: {}", updatedFilm);
        return updatedFilm;
    }

    @Override
    public Optional<Film> getById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null.");
        }

        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAllFilms() {
        return List.copyOf(films.values());
    }

    @Override
    public boolean isUserExist(int id) {
        return userStorage.isUserExist(id);
    }
}
