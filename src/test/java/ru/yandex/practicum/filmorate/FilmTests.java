package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.DAOImpl.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmService.class, FilmDbStorage.class, LikeDbStorage.class, GenreDbStorage.class, MpaDbStorage.class, UserDbStorage.class, EventDbStorage.class, DirectorDbStorage.class})
class FilmTests {
    private final FilmService filmService;

    private final FilmDbStorage filmStorage;
    private final LikeDbStorage likeStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;
    private final UserDbStorage userStorage;

    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private Film film1;
    private Film film2;
    private Film film3;

    @BeforeEach
    public void beforeEach() {
        user1 = new User();
        user1.setEmail("glasha@example.com");
        user1.setLogin("glasha");
        user1.setName("Глаша");
        user1.setBirthday(LocalDate.of(1995, 4, 20));

        user2 = new User();
        user2.setEmail("timofey@example.com");
        user2.setLogin("timofey");
        user2.setName("Тимофей");
        user2.setBirthday(LocalDate.of(1990, 7, 15));

        user3 = new User();
        user3.setEmail("pavel@example.com");
        user3.setLogin("pavel");
        user3.setName("Павел");
        user3.setBirthday(LocalDate.of(1985, 3, 10));

        user4 = new User();
        user4.setEmail("anna@example.com");
        user4.setLogin("anna");
        user4.setName("Анна");
        user4.setBirthday(LocalDate.of(1992, 8, 25));


        userStorage.createUser(user1);
        userStorage.createUser(user2);
        userStorage.createUser(user3);
        userStorage.createUser(user4);

        MPA mpa4 = new MPA(4L, "R", "лицам до 17 лет просматривать фильм можно только в присутствии взрослого");
        MPA mpa3 = new MPA(3L, "PG-13", "детям до 13 лет просмотр не желателен");
        Genre genre1 = new Genre(1L, "Комедия");
        Genre genre2 = new Genre(2L, "Драма");

        film1 = new Film();
        film1.setId(1L);
        film1.setName("Interstellar");
        film1.setDescription("A journey through space and time to save humanity");
        film1.setReleaseDate(LocalDate.of(2014, 11, 7));
        film1.setDuration(169L);
        film1.setGenres(Set.of(genre1, genre2));
        film1.setMpa(mpa4);

        film2 = new Film();
        film2.setId(2L);
        film2.setName("Silo");
        film2.setDescription("A dystopian future where humanity lives underground");
        film2.setReleaseDate(LocalDate.of(2023, 5, 5));
        film2.setDuration(120L);
        film2.setGenres(Set.of(genre2));
        film2.setMpa(mpa3);

        film3 = new Film();
        film3.setId(4L);
        film3.setName("Brave New World");
        film3.setDescription("A dystopian society where everyone is conditioned for their role, and individuality is suppressed.");
        film3.setReleaseDate(LocalDate.of(2020, 7, 15));
        film3.setDuration(480L);
        film3.setGenres(Set.of(genre2));
        film3.setMpa(mpa3);

        filmService.addFilm(film1);
        filmService.addFilm(film2);
        filmService.addFilm(film3);
    }

    @Test
    public void testAddFilm() {
        Film addedFilm = filmService.addFilm(film1);

        assertNotNull(addedFilm, "Добавленный фильм не должен быть null");
        assertEquals(film1.getId(), addedFilm.getId(), "ID добавленного фильма должен совпадать");

        Film filmFromStorage = filmService.getFilmById(film1.getId());
        assertNotNull(filmFromStorage, "Фильм должен существовать в хранилище");

        assertEquals(film1.getName(), filmFromStorage.getName(), "Название фильма должно совпадать");
        assertEquals(film1.getDescription(), filmFromStorage.getDescription(), "Описание фильма должно совпадать");
        assertEquals(film1.getReleaseDate(), filmFromStorage.getReleaseDate(), "Дата выхода фильма должна совпадать");
        assertEquals(film1.getDuration(), filmFromStorage.getDuration(), "Продолжительность фильма должна совпадать");
        assertEquals(film1.getMpa(), filmFromStorage.getMpa(), "MPA фильма должен совпадать");
        assertEquals(film1.getGenres(), filmFromStorage.getGenres(), "Жанры фильма должны совпадать");
    }

    @Test
    public void testUpdateFilm() {
        film1.setName("Updated Film");
        filmService.updateFilm(film1);

        Film updatedFilm = filmService.getFilmById(film1.getId());
        assertThat(updatedFilm).isNotNull();
        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");

        film1.setGenres(Set.of(new Genre(4L, "Триллер")));
        filmService.updateFilm(film1);

        Film filmWithUpdatedGenres = filmService.getFilmById(film1.getId());
        assertThat(filmWithUpdatedGenres).isNotNull();
        assertThat(filmWithUpdatedGenres.getGenres())
                .isNotEmpty()
                .extracting(Genre::getName)
                .contains("Триллер");
    }

    @Test
    public void testDeleteFilm() {
        filmStorage.deleteFilmById(film1.getId());

        Optional<Film> retrievedFilm = filmStorage.getFilmById(film1.getId());
        assertThat(retrievedFilm).isEmpty();
    }

    @Test
    public void testGetFilmById() {
        Optional<Film> retrievedFilm = filmStorage.getFilmById(film1.getId());

        assertThat(retrievedFilm).isPresent().hasValueSatisfying(f ->
                assertThat(f).hasFieldOrPropertyWithValue("id", film1.getId())
        );
    }

    @Test
    public void testGetAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        assertThat(films).hasSize(3);
    }

    @Test
    public void testGetGenresByFilmId() {
        Set<Genre> genre = genreStorage.getGenresByFilmId(film1.getId());
        assertThat(genre).isNotEmpty();
        assertThat(genre.iterator().next()).hasFieldOrPropertyWithValue("name", "Комедия");

        assertThat(genreStorage.getGenresByFilmId(999L)).isEmpty();
    }

    @Test
    public void testGetMPAById() {
        MPA mpa = mpaStorage.getMpaById(film1.getMpa().getId()).orElseThrow();
        assertThat(mpa).hasFieldOrPropertyWithValue("name", "R");

        assertThat(mpaStorage.getMpaById(999L)).isEmpty();
    }

    @Test
    public void testFindFilmsBySubstring() {
        List<Film> filmsByName = filmStorage.findFilmsBySubstring("Inter", "name");
        assertThat(filmsByName).hasSize(1).first().hasFieldOrPropertyWithValue("name", "Interstellar");

        List<Film> filmsByDescription = filmStorage.findFilmsBySubstring("space", "description");
        assertThat(filmsByDescription).hasSize(1).first().hasFieldOrPropertyWithValue("description", "A journey through space and time to save humanity");

        Throwable thrown = catchThrowable(() -> filmStorage.findFilmsBySubstring("test", "invalid"));
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("by");
    }

    @Test
    public void testGetCommonFilms() {
        // Лайки пользователей
        likeStorage.like(film3.getId(), user1.getId());
        likeStorage.like(film3.getId(), user2.getId());

        likeStorage.like(film2.getId(), user1.getId());
        likeStorage.like(film2.getId(), user2.getId());
        likeStorage.like(film2.getId(), user3.getId());

        likeStorage.like(film1.getId(), user1.getId());
        likeStorage.like(film1.getId(), user2.getId());
        likeStorage.like(film1.getId(), user3.getId());
        likeStorage.like(film1.getId(), user4.getId());

        // Получение общих фильмов
        List<Film> commonFilms = filmStorage.getCommonFilms(user1.getId(), user2.getId());

        // Проверка размеров списка
        assertThat(commonFilms).hasSize(3);

        // Проверка порядка сортировки по популярности
        assertThat(commonFilms.get(0).getName()).isEqualTo("Interstellar");
        assertThat(commonFilms.get(1).getName()).isEqualTo("Silo");
        assertThat(commonFilms.get(2).getName()).isEqualTo("Brave New World");
    }

    @Test
    public void testIsFilmExist() {
        assertThat(filmStorage.isFilmExist(film1.getId())).isTrue();
        assertThat(filmStorage.isFilmExist(999L)).isFalse();
    }

    @Test
    public void testGetGenresForAllFilms() {
        Map<Long, Set<Genre>> genresMap = genreStorage.getGenresForAllFilms();
        assertThat(genresMap).containsKeys(film1.getId(), film2.getId());
        assertThat(genresMap.get(film1.getId())).extracting(Genre::getName).contains("Комедия", "Драма");
    }

    @Test
    public void testGetAllGenres() {
        List<Genre> genres = genreStorage.getAllGenres();
        assertThat(genres).isNotEmpty();
    }

    @Test
    public void testGetAllMPAs() {
        List<MPA> mpas = mpaStorage.getAllMpas();
        assertThat(mpas).isNotEmpty();
    }
}
