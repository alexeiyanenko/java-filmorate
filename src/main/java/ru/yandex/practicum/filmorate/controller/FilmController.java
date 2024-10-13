package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@Controller
@RequestMapping("/films")
public class FilmController {

    private int currentId = 1;
    private final Map<Integer, Film> films = new HashMap<>();

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        film.setId(currentId++);
        films.put(film.getId(), film);
        log.info("Фильм добавлен: {}", film);
        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Object> updateFilm(@Valid @RequestBody Film updatedFilm) {
        if (films.containsKey(updatedFilm.getId())) {
            films.put(updatedFilm.getId(), updatedFilm);
            log.info("Фильм обновлён: {}", updatedFilm);
            return ResponseEntity.ok(updatedFilm);
        } else {
            log.warn("Фильм с ID {} не найден для обновления.", updatedFilm.getId());
            return new ResponseEntity<>(new ErrorResponse("Фильм не найден"), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        return ResponseEntity.ok(List.copyOf(films.values()));
    }
}
