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
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.DAOImpl.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FriendshipDbStorage.class,EventDbStorage.class,UserDbStorage.class,
        UserService.class, LikeDbStorage.class, FilmDbStorage.class})
public class UserTests {
    private final FriendshipDbStorage friendshipStorage;
    private final UserDbStorage userStorage;
    private final UserService userService;
    private final LikeDbStorage likeStorage;
    private final FilmDbStorage filmStorage;
    private User user1;
    private User user2;
    private User user3;
    private Film film;

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

        user3 = new User();
        user3.setId(3L);
        user3.setEmail("smth@example.com");
        user3.setLogin("smth");
        user3.setName("Саша");
        user3.setBirthday(LocalDate.of(1994, 9, 15));

        MPA mpa = new MPA(4L, "R", "лицам до 17 лет просматривать фильм можно только в присутствии взрослого");
        Genre genre1 = new Genre(1L, "Комедия");
        film = new Film();
        film.setId(1L);
        film.setName("Interstellar");
        film.setDescription("A journey through space and time to save humanity");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169L);
        film.setGenres(Set.of(genre1));
        film.setMpa(mpa);

        filmStorage.addFilm(film);
        userStorage.createUser(user1);
        userStorage.createUser(user2);
        userStorage.createUser(user3);

    }

    @Test
    public void testGetCommonFriends() {
        friendshipStorage.addFriendship(user1, user2);
        friendshipStorage.addFriendship(user2, user1);
        friendshipStorage.addFriendship(user1, user3);
        friendshipStorage.addFriendship(user2, user3);
        List<User> friends = userService.getCommonFriends(user1.getId(), user2.getId());
        assertThat(friends).isNotEmpty();
        assertThat(friends).size().isEqualTo(1);
    }
    @Test
    public void testGetRecommendations() {

        likeStorage.like(film.getId(),user2.getId());

        List<Film> recs = userService.getRecommendations(user1.getId());
        assertThat(recs).isNotEmpty();
        assertThat(recs).size().isEqualTo(1);
    }
}
