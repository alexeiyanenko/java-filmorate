package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
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

        // Обновление жанров, если они указаны
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            updatedFilm = genreStorage.updateGenres(updatedFilm);
        }

        return Optional.ofNullable(updatedFilm);
    }

    public Film getFilmById(long filmId) {
        // Проверка существования фильма
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NoSuchElementException("Фильм с ID " + filmId + " не найден."));

        // Получение MPA
        if (film.getMpa() != null) {
            MPA mpa = mpaStorage.getMpaById(film.getMpa().getId())
                    .orElseThrow(() -> new ValidationException("Некорректный MPA ID: " + film.getMpa().getId()));
            film.setMpa(mpa);
        }

        // Получение жанров
        Set<Genre> genres = genreStorage.getGenresByFilmId(film.getId());
        film.setGenres(genres);

        return film;
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
        Map<Long, Set<Genre>> genresMap = genreStorage.getGenresForAllFilms();

        // Создаём новую коллекцию фильмов с лайками и жанрами
        List<Film> enrichedFilms = films.stream()
                .map(film -> {
                    // Получаем лайки и жанры для текущего фильма
                    Set<Long> likes = likesMap.getOrDefault(film.getId(), new HashSet<>());
                    Set<Genre> genres = genresMap.getOrDefault(film.getId(), new HashSet<>());

                    // Создаём новый объект фильма с лайками и жанрами
                    Film enrichedFilm = new Film();
                    enrichedFilm.setId(film.getId());
                    enrichedFilm.setName(film.getName());
                    enrichedFilm.setDescription(film.getDescription());
                    enrichedFilm.setReleaseDate(film.getReleaseDate());
                    enrichedFilm.setDuration(film.getDuration());
                    enrichedFilm.setMpa(film.getMpa());
                    enrichedFilm.setGenres(genres);

                    enrichedFilm.getLikes().addAll(likes);

                    return enrichedFilm;
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
        return filmStorage.findFilmsBySubstring(query, by);
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        if (!userStorage.isUserExist(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        if (!userStorage.isUserExist(friendId)) {
            throw new NotFoundException("Друг с ID " + friendId + " не найден.");
        }

        List<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);

        Map<Long, Set<Genre>> genresMap = genreStorage.getGenresForAllFilms();

        commonFilms.forEach(film -> film.setGenres(genresMap.getOrDefault(film.getId(), new HashSet<>())));

        return commonFilms;
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
}