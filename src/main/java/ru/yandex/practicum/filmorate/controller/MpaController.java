package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
@Validated
@RequiredArgsConstructor
public class MpaController {
    private final FilmService filmService;

    @GetMapping
    public List<MPA> getAllMpas() {
        log.debug("Запрос на получение всех рейтингов МПА");
        return filmService.getAllMPAs();
    }

    @GetMapping("/{id}")
    public MPA getMpaById(@PathVariable long id) {
        log.debug("Запрос на получение рейтинга МПА по id: {}", id);
        return filmService.getMPAById(id);
    }
}
