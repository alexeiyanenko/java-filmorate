package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.DAOImpl.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.DAOImpl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.DAOImpl.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({EventDbStorage.class, UserDbStorage.class, FilmDbStorage.class})
class EventDbStorageTests {
    private final EventDbStorage eventStorage;
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    private User user1;
    private Film film1;

    @BeforeEach
    public void beforeEach() {
        user1 = new User();
        user1.setEmail("glasha@example.com");
        user1.setLogin("glasha");
        user1.setName("Глаша");
        user1.setBirthday(LocalDate.of(1995, 4, 20));

        userStorage.createUser(user1);

        MPA mpa4 = new MPA(4L, "R", "лицам до 17 лет просматривать фильм можно только в присутствии взрослого");
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

        filmStorage.addFilm(film1);
    }

    @Test
    public void testCreateEvent() {
        eventStorage.createEvent(user1.getId(), Event.EventType.LIKE, Event.Operation.ADD, 1L);

        List<Event> createdEvents = eventStorage.getAllEventsById(user1.getId());

        Assertions.assertThat(createdEvents).hasSize(1);
    }
}

