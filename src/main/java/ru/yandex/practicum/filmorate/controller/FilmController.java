package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/films")
@Slf4j
@Validated
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        log.debug("Запрос на добавление фильма: {}", film);
        Film addefFilm = filmService.addFilm(film);
        return new ResponseEntity<>(addefFilm, HttpStatus.CREATED);
    }

    @PutMapping
    public Optional<Film> updateFilm(@Valid @RequestBody Film film) {
        log.debug("Запрос на обновление фильма: {}", film);
        return filmService.updateFilm(film);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilmById(@PathVariable long filmId) {
        log.debug("Запрос на удаление фильма с id: {}", filmId);
        filmService.deleteFilmById(filmId);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("Запрос на получение всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable long filmId) {
        log.debug("Запрос на получение фильма по id: {}", filmId);
        return filmService.getFilmById(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable long id, @PathVariable long userId) {
        log.debug("Запрос на добавление лайка фильму id={} от пользователя id={}", id, userId);
        filmService.like(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void unlike(@PathVariable long id, @PathVariable long userId) {
        log.debug("Запрос на удаление лайка у фильма id={} от пользователя id={}", id, userId);
        filmService.unlike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count,
                                       @RequestParam(required = false) Long genreId,
                                       @RequestParam(required = false) Integer year) {
        log.debug("Запрос на популярные фильмы. Количество={}, жанр={}, год={}", count, genreId, year);
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/search")
    public List<Film> findFilmsBySubstring(@RequestParam String query, @RequestParam String by) {
        log.debug("Запрос на поиск фильмов с подстрокой '{}' в '{}'", query, by);
        return filmService.findFilmsBySubstring(query, by);
    }

    @GetMapping("/common")
    public List<Film> findCommonFilms(@RequestParam long userId, @RequestParam long friendId) {
        log.debug("Запрос на общие фильмы для пользователей id={} и id={}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }
}