package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Event;

import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.LikeStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.validation.ValidationException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("mpaDbStorage")
    private final MpaStorage mpaStorage;
    @Qualifier("genreDbStorage")
    private final GenreStorage genreStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    @Qualifier("likeDbStorage")
    private final LikeStorage likeStorage;
    @Qualifier("eventDbStorage")
    private final EventStorage eventStorage;
    @Qualifier("directorDbStorage")
    private final DirectorStorage directorStorage;

    public Film addFilm(Film film) {
        // Получение и проверка существования MPA
        MPA mpa = mpaStorage.getMpaById(film.getMpa().getId())
                .orElseThrow(() -> new ValidationException("Некорректный MPA ID: " + film.getMpa().getId()));

        film.setMpa(mpa);

        // Добавление фильма в хранилище
        Film savedFilm = filmStorage.addFilm(film)
                .orElseThrow(() -> new ValidationException("Не удалось создать фильм. Проверьте входные данные."));

        // Логика обновления жанров, если они указаны
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            savedFilm = genreStorage.updateGenres(savedFilm);
        }
        // Логика обновления режиссеров, если они указаны
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            savedFilm = directorStorage.addDirectorToFilm(film);
        }

        return savedFilm;
    }

    public Optional<Film> updateFilm(Film film) {
        // Проверка существования MPA
        if (film.getMpa() != null) {
            MPA mpa = mpaStorage.getMpaById(film.getMpa().getId())
                    .orElseThrow(() -> new ValidationException("Некорректный MPA ID: " + film.getMpa().getId()));
            film.setMpa(mpa);
        }

        // Проверка существования фильма
        if (!filmStorage.isFilmExist(film.getId())) {
            throw new NoSuchElementException("Фильм с ID " + film.getId() + " не найден.");
        }

        // Обновление фильма в хранилище
        Film updatedFilm = filmStorage.updateFilm(film)
                .orElseThrow(() -> new ValidationException("Не удалось обновить фильм. Проверьте входные данные."));

        updatedFilm = genreStorage.updateGenres(updatedFilm);

        updatedFilm = directorStorage.updateDirectorToFilm(updatedFilm);

        return Optional.ofNullable(updatedFilm);
    }

    public Film getFilmById(long filmId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NoSuchElementException("Фильм с ID " + filmId + " не найден."));

        return enrichFilm(film);
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
        List<Film> films = filmStorage.getAllFilms();

        return films.stream()
                .map(this::enrichFilm)
                .collect(Collectors.toList());
    }

    public List<Film> getPopularFilms(int count, Long genreId, Integer year) {
        // Получаем все фильмы из хранилища
        List<Film> films = filmStorage.getAllFilms();

        // Загружаем все лайки и жанры из базы данных
        Map<Long, Set<Long>> likesMap = likeStorage.getAllLikes();
        Map<Long, Set<Genre>> genresMap = genreStorage.getGenresForAllFilms();

        // Создаём новую коллекцию фильмов и обогащаем
        List<Film> enrichedFilms = films.stream()
                .map(this::enrichFilm) // Обогащаем фильм
                .peek(film -> {
                    // Добавляем лайки из карты лайков
                    Set<Long> likes = likesMap.getOrDefault(film.getId(), new HashSet<>());
                    film.getLikes().addAll(likes);

                    // Устанавливаем жанры из карты жанров (если нужно обновить)
                    Set<Genre> genres = genresMap.getOrDefault(film.getId(), new HashSet<>());
                    film.setGenres(genres);
                })
                .toList();

        // Фильтрация и сортировка
        return enrichedFilms.stream()
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

        List<Film> foundFilms = filmStorage.findFilmsBySubstring(query, by);

        return foundFilms.stream()
                .map(this::enrichFilm)
                .collect(Collectors.toList());
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        if (!userStorage.isUserExist(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (!userStorage.isUserExist(friendId)) {
            throw new NotFoundException("Друг с ID " + friendId + " не найден.");
        }

        List<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);

        return commonFilms.stream()
                .map(this::enrichFilm)
                .collect(Collectors.toList());
    }

    public void like(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден."));
        if (!userStorage.isUserExist(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }

        likeStorage.like(filmId, userId);
        eventStorage.createEvent(userId, Event.EventType.LIKE, Event.Operation.ADD, filmId);
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
        eventStorage.createEvent(userId, Event.EventType.LIKE, Event.Operation.REMOVE, filmId);
        log.info("Пользователь с ID {} убрал лайк у фильма с ID {}", userId, filmId);
    }

    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(Long id) {
        return genreStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден."));
    }

    public List<MPA> getAllMPAs() {
        return mpaStorage.getAllMpas();
    }

    public MPA getMPAById(Long id) {
        return mpaStorage.getMpaById(id)
                .orElseThrow(() -> new NotFoundException("MPA с ID " + id + " не найден."));
    }

    public List<Film> getDirectorFilms(Long directorId, String sortBy) {
        if (!directorStorage.isDirectorExist(directorId)) {
            throw new NotFoundException("Режиссер с ID " + directorId + " не найден.");
        }

        List<Film> films = filmStorage.getDirectorFilms(directorId, sortBy);

        return films.stream()
                .map(this::enrichFilm)
                .collect(Collectors.toList());
    }

    private Film enrichFilm(Film film) {
        // Обогащаем жанрами
        film.setGenres(genreStorage.getGenresByFilmId(film.getId()));

        // Обогащаем режиссерами
        film.setDirectors(directorStorage.getDirectorsByFilmId(film.getId()));

        // Обогащаем MPA
        if (film.getMpa() != null) {
            MPA mpa = mpaStorage.getMpaById(film.getMpa().getId())
                    .orElseThrow(() -> new ValidationException("Некорректный MPA ID: " + film.getMpa().getId()));
            film.setMpa(mpa);
        }

        return film;
    }
}