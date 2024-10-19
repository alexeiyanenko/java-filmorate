package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTests {
    private final FilmDbStorage filmStorage;

    @BeforeEach
    public void setUp() {
        Film film = new Film();
        film.setId(1);
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        filmStorage.addFilm(film);
    }

    @Test
    public void testFindFilmById() {
        Optional<Film> filmOptional = filmStorage.getById(1);
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    public void testAddFilm() {
        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("New description");
        newFilm.setReleaseDate(LocalDate.of(2021, 5, 15));
        newFilm.setDuration(90);

        Film createdFilm = filmStorage.addFilm(newFilm);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isGreaterThan(1);
        assertThat(createdFilm.getName()).isEqualTo("New Film");
    }

    @Test
    public void testUpdateFilm() {
        Film filmToUpdate = new Film();
        filmToUpdate.setId(1);
        filmToUpdate.setName("Updated Film");
        filmToUpdate.setDescription("Updated description");
        filmToUpdate.setReleaseDate(LocalDate.of(2020, 1, 1));
        filmToUpdate.setDuration(150);

        filmStorage.updateFilm(filmToUpdate);

        Optional<Film> updatedFilmOptional = filmStorage.getById(1);
        assertThat(updatedFilmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("name", "Updated Film")
                );
    }
}
