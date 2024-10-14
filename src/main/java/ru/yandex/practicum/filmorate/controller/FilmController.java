package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        return ResponseEntity.status(HttpStatus.CREATED).body(filmService.addFilm(film));
    }

    @PutMapping
    public ResponseEntity<Film> updateUser(@RequestBody @Valid Film updatedFilm) {
        return ResponseEntity.ok(filmService.updateFilm(updatedFilm));
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllUsers() {
        return ResponseEntity.ok(filmService.getAllFilms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getById(@PathVariable int id) {
        return ResponseEntity.ok(filmService.getById(id));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> like(@PathVariable int id, @PathVariable int userId) {
        filmService.like(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> unlike(@PathVariable int id, @PathVariable int userId) {
        filmService.unlike(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(filmService.getPopularFilms(count));
    }
}
