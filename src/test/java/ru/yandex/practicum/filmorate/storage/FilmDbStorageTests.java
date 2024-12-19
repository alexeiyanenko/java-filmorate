package ru.yandex.practicum.filmorate.storage;

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
import ru.yandex.practicum.filmorate.storage.DAOImpl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.DAOImpl.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.DAOImpl.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class, LikeDbStorage.class})
class FilmDbStorageTests {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final LikeDbStorage likeStorage;

    private User user1;
    private User user2;
    private Film film1;
    private Film film2;

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

        userStorage.createUser(user1);
        userStorage.createUser(user2);

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

        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);
    }

    @Test
    public void testAddFilm() {
        Optional<Film> retrievedFilm = filmStorage.getFilmById(film1.getId());
        assertThat(retrievedFilm).isPresent().hasValueSatisfying(f ->
                assertThat(f).hasFieldOrPropertyWithValue("name", "Interstellar")
        );
        assertThat(filmStorage.getGenresByFilmId(film1.getId())).isNotEmpty();
    }

    @Test
    public void testUpdateFilm() {
        film1.setName("Updated Film");
        filmStorage.updateFilm(film1);

        Optional<Film> updatedFilm = filmStorage.getFilmById(film1.getId());
        assertThat(updatedFilm).isPresent().hasValueSatisfying(f ->
                assertThat(f).hasFieldOrPropertyWithValue("name", "Updated Film")
        );

        film1.setGenres(Set.of(new Genre(4L, "Триллер")));
        filmStorage.updateFilm(film1);
        assertThat(filmStorage.getGenresByFilmId(film1.getId())).extracting(Genre::getName).contains("Триллер");
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
        assertThat(films).hasSize(2);
    }

    @Test
    public void testGetGenresByFilmId() {
        Set<Genre> genre = filmStorage.getGenresByFilmId(film1.getId());
        assertThat(genre).isNotEmpty();
        assertThat(genre.iterator().next()).hasFieldOrPropertyWithValue("name", "Комедия");

        assertThat(filmStorage.getGenresByFilmId(999L)).isEmpty();
    }

    @Test
    public void testGetMPAById() {
        MPA mpa = filmStorage.getMPAById(film1.getMpa().getId()).orElseThrow();
        assertThat(mpa).hasFieldOrPropertyWithValue("name", "R");

        assertThat(filmStorage.getMPAById(999L)).isEmpty();
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
        likeStorage.like(film1.getId(), user1.getId());
        likeStorage.like(film1.getId(), user2.getId());
        likeStorage.like(film2.getId(), user1.getId());

        List<Film> commonFilms = filmStorage.getCommonFilms(user1.getId(), user2.getId());
        assertThat(commonFilms).hasSize(1).first().hasFieldOrPropertyWithValue("name", "Interstellar");
    }

    @Test
    public void testIsFilmExist() {
        assertThat(filmStorage.isFilmExist(film1.getId())).isTrue();
        assertThat(filmStorage.isFilmExist(999L)).isFalse();
    }

    @Test
    public void testGetGenresForAllFilms() {
        Map<Long, Set<Genre>> genresMap = filmStorage.getGenresForAllFilms();
        assertThat(genresMap).containsKeys(film1.getId(), film2.getId());
        assertThat(genresMap.get(film1.getId())).extracting(Genre::getName).contains("Комедия", "Драма");
    }

    @Test
    public void testGetAllGenres() {
        List<Genre> genres = filmStorage.getAllGenres();
        assertThat(genres).isNotEmpty();
    }

    @Test
    public void testGetAllMPAs() {
        List<MPA> mpas = filmStorage.getAllMPAs();
        assertThat(mpas).isNotEmpty();
    }
}
