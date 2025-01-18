package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.DAOImpl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.DAOImpl.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.DAOImpl.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, ReviewDbStorage.class})
class ReviewDbStorageTests {

    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final ReviewDbStorage reviewDbStorage;

    private User user;
    private User user2;
    private Film film1;
    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail("glasha@example.com");
        user.setLogin("glasha");
        user.setName("Глаша");
        user.setBirthday(LocalDate.of(1995, 4, 20));
        user2 = new User();
        user2.setEmail("timofey@example.com");
        user2.setLogin("timofey");
        user2.setName("Тимофей");
        user2.setBirthday(LocalDate.of(1990, 7, 15));


        userDbStorage.createUser(user);
        userDbStorage.createUser(user2);

        MPA mpa3 = new MPA(3L, "PG-13", "детям до 13 лет просмотр не желателен");
        Genre genre1 = new Genre(1L, "Комедия");

        film1 = new Film();
        film1.setName("Interstellar");
        film1.setDescription("A journey through space and time to save humanity");
        film1.setReleaseDate(LocalDate.of(2014, 11, 7));
        film1.setDuration(169L);
        film1.setGenres(Set.of(genre1));
        film1.setMpa(mpa3);
        filmDbStorage.addFilm(film1);
    }
    @Test
    public void testAddReview() {
        Review review = new Review();
        review.setContent("Great movie!");
        review.setIsPositive(true);
        review.setUserId(user.getId());
        review.setFilmId(film1.getId());
        Review createdReview = reviewDbStorage.addReview(review);

        assertThat(createdReview).isNotNull();
        assertThat(createdReview.getReviewId()).isNotNull();
        assertThat(createdReview.getContent()).isEqualTo("Great movie!");
        assertThat(createdReview.getIsPositive()).isTrue();
    }

    @Test
    public void testUpdateReview() {
        Review review = new Review();
        review.setContent("Amazing film!");
        review.setIsPositive(false);
        review.setUserId(user.getId());
        review.setFilmId(film1.getId());
        Review createdReview = reviewDbStorage.addReview(review);
        createdReview.setContent("Updated review content");
        createdReview.setIsPositive(true);

        Review updatedReview = reviewDbStorage.updateReview(createdReview);

        assertThat(updatedReview.getContent()).isEqualTo("Updated review content");
        assertThat(updatedReview.getIsPositive()).isTrue();
    }

    @Test
    public void testDeleteReview() {
        Review review = new Review();
        review.setContent("Review to delete");
        review.setIsPositive(true);
        review.setUserId(user.getId());
        review.setFilmId(film1.getId());
        Review createdReview = reviewDbStorage.addReview(review);
        reviewDbStorage.deleteReview(createdReview.getReviewId());

        assertThrows(EmptyResultDataAccessException.class, () -> reviewDbStorage.getReviewById(createdReview.getReviewId()));
    }

    @Test
    public void testGetReviewById() {
        Review review = new Review();
        review.setContent("Specific review");
        review.setIsPositive(true);
        review.setUserId(user.getId());
        review.setFilmId(film1.getId());
        Review createdReview = reviewDbStorage.addReview(review);

        Review retrievedReview = reviewDbStorage.getReviewById(createdReview.getReviewId());

        assertThat(retrievedReview).isNotNull();
        assertThat(retrievedReview.getReviewId()).isEqualTo(createdReview.getReviewId());
    }

    @Test
    public void testGetAllReviewsForFilmWithCount() {
        Review review1 = new Review();
        review1.setContent("Review 1");
        review1.setIsPositive(true);
        review1.setUserId(user.getId());
        review1.setFilmId(film1.getId());
        reviewDbStorage.addReview(review1);

        Review review2 = new Review();
        review2.setContent("Review 2");
        review2.setIsPositive(false);
        review2.setUserId(user2.getId());
        review2.setFilmId(film1.getId());

        reviewDbStorage.addReview(review2);

        List<Review> reviews = reviewDbStorage.getAllReviews(film1.getId(), 1L);

        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getFilmId()).isEqualTo(film1.getId());
    }

    @Test
    public void testGetAllReviewsForFilm() {
        Review review1 = new Review();
        review1.setContent("General review 1");
        review1.setIsPositive(true);
        review1.setUserId(user.getId());
        review1.setFilmId(film1.getId());
        reviewDbStorage.addReview(review1);

        Review review2 = new Review();
        review2.setContent("General review 2");
        review2.setIsPositive(false);
        review2.setUserId(user2.getId());
        review2.setFilmId(film1.getId());

        reviewDbStorage.addReview(review2);

        List<Review> reviews = reviewDbStorage.getAllReviews(1L);

        assertThat(reviews).hasSize(2);
        assertThat(reviews.get(0).getFilmId()).isEqualTo(1L);
        assertThat(reviews.get(1).getFilmId()).isEqualTo(1L);
    }

    @Test
    public void testGetAllReviews() {
        Review review1 = new Review();
        review1.setContent("General review 1");
        review1.setIsPositive(true);
        review1.setUserId(user.getId());
        review1.setFilmId(film1.getId());
        reviewDbStorage.addReview(review1);

        Review review2 = new Review();
        review2.setContent("General review 2");
        review2.setIsPositive(false);
        review2.setUserId(user2.getId());
        review2.setFilmId(film1.getId());

        reviewDbStorage.addReview(review2);

        List<Review> reviews = reviewDbStorage.getAllReviews();

        assertThat(reviews).hasSize(2);
    }
}

