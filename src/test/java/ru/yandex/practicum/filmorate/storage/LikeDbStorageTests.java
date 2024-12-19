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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({LikeDbStorage.class, UserDbStorage.class, FilmDbStorage.class})
class LikeDbStorageTests {
    private final LikeDbStorage likeStorage;
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    private User user1;
    private User user2;
    private Film film1;
    private Film film2;

    @BeforeEach
    public void beforeEach() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("glasha@example.com");
        user1.setLogin("glasha");
        user1.setName("Глаша");
        user1.setBirthday(LocalDate.of(1995, 4, 20));

        user2 = new User();
        user2.setId(2L);
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
    public void testLikeFilm() {
        boolean liked = likeStorage.like(film1.getId(), user1.getId());
        assertThat(liked).isTrue();
    }

    @Test
    public void testUnlikeFilm() {
        likeStorage.like(film1.getId(), user1.getId());
        boolean unliked = likeStorage.unlike(film1.getId(), user1.getId());
        assertThat(unliked).isTrue();
    }

    @Test
    public void testGetLikesByFilmId() {
        List<Long> likes = likeStorage.getLikesByFilmId(film1.getId());
        assertThat(likes).isNotNull();
    }

    @Test
    public void testGetAllLikes() {
        likeStorage.like(film1.getId(), user1.getId());
        likeStorage.like(film1.getId(), user2.getId());
        likeStorage.like(film2.getId(), user1.getId());

        Map<Long, Set<Long>> allLikes = likeStorage.getAllLikes();

        assertThat(allLikes).isNotNull();
        assertThat(allLikes).containsKeys(film1.getId(), film2.getId());
        assertThat(allLikes.get(film1.getId())).containsExactlyInAnyOrder(user1.getId(), user2.getId());
        assertThat(allLikes.get(film2.getId())).containsExactly(user1.getId());
    }
}

