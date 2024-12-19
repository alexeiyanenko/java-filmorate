package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage2")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    @Qualifier("likeDbStorage")
    private final LikeStorage likeStorage;
    @Qualifier("eventDbStorage")
    private final EventStorage eventStorage;

    public Film addFilm(Film film) {
        try {
            MPA mpa = filmStorage.getMPAById(film.getMPARating().getId())
                    .orElseThrow(() -> new ValidationException("Некорректный MPA ID: " + film.getMPARating().getId()));
            film.setMPARating(mpa);

            // Добавление фильма в хранилище
            return filmStorage.addFilm(film)
                    .orElseThrow(() -> new ValidationException("Не удалось создать фильм. Проверьте входные данные."));
        } catch (ValidationException e) {
            log.error("Ошибка валидации: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Неизвестная ошибка: {}", e.getMessage(), e);
            throw new RuntimeException("Произошла ошибка при создании фильма.", e);
        }
    }

    public Optional<Film> updateFilm(Film updatedFilm) {
        return filmStorage.updateFilm(updatedFilm);
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден."));
    }

    public void deleteFilmById(Long id) {
        if (!filmStorage.deleteFilmById(id)) {
            throw new NotFoundException("Фильм с ID " + id + " не найден для удаления.");
        }
        log.info("Фильм с ID {} удалён.", id);
    }

    public boolean isFilmExist(Long id) {
        return filmStorage.isFilmExist(id);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public List<Film> getPopularFilms(int count, Long genreId, Integer year) {
        // Получаем все фильмы из хранилища
        List<Film> films = filmStorage.getAllFilms();

        // Загружаем все лайки и жанры из базы данных
        Map<Long, Set<Long>> likesMap = likeStorage.getAllLikes();
        Map<Long, Set<Genre>> genresMap = filmStorage.getGenresForAllFilms();

        // Добавляем лайки к каждому фильму
        for (Film film : films) {
            Set<Long> likes = likesMap.getOrDefault(film.getId(), new HashSet<>());
            film.setLikes(likes);

            Set<Genre> genres = genresMap.getOrDefault(film.getId(), new HashSet<>());
            film.setGenres(genres);
        }

        // Фильтрация и сортировка
        return films.stream()
                // Фильтруем фильмы по жанру
                .filter(film -> genreId == null || genreId == 0 ||
                        film.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId)))
                // Фильтруем фильмы по году
                .filter(film -> year == null || year == 0 || film.getReleaseDate().getYear() == year)
                // Сортируем фильмы по количеству лайков
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                // Ограничиваем количество фильмов
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> findFilmsBySubstring(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Запрос не может быть пустым.");
        }
        return filmStorage.findFilmsBySubstring(query, by);
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        if (!userStorage.isUserExist(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (!userStorage.isUserExist(friendId)) {
            throw new NotFoundException("Друг с ID " + friendId + " не найден.");
        }
        return filmStorage.getCommonFilms(userId, friendId);
    }


    public void like(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден."));
        if (!userStorage.isUserExist(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (likeStorage.getLikesByFilmId(filmId).contains((long) userId)) {
            throw new IllegalStateException("Пользователь " + userId + " уже лайкнул фильм с ID " + filmId);
        }
        likeStorage.like(filmId, userId);
        eventStorage.createEvent(userId, "LIKE", "ADD", filmId);
        log.info("Пользователь с ID {} лайкнул фильм с ID {}", userId, filmId);
    }

    public void unlike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден."));
        if (!userStorage.isUserExist(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (!likeStorage.getLikesByFilmId(filmId).contains((long) userId)) {
            throw new IllegalStateException("Пользователь с ID " + userId + " не лайкал фильм с ID " + filmId);
        }
        likeStorage.unlike(filmId, userId);
        eventStorage.createEvent(userId, "LIKE", "REMOVE", filmId);
        log.info("Пользователь с ID {} убрал лайк у фильма с ID {}", userId, filmId);
    }

    public List<Genre> getAllGenres() {
        return filmStorage.getAllGenres();
    }

    public Genre getGenreById(Long id) {
        return filmStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден."));
    }

    public List<MPA> getAllMPAs() {
        return filmStorage.getAllMPAs();
    }

    public MPA getMPAById(Long id) {
        return filmStorage.getMPAById(id)
                .orElseThrow(() -> new NotFoundException("MPA с ID " + id + " не найден."));
    }
}