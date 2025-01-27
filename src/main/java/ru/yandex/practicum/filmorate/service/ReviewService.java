package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    @Qualifier("reviewDbStorage")
    private final ReviewStorage reviewStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("eventStorage")
    private final EventStorage eventStorage;

    public Review addReview(Review review) {

        //Проверяем, есть ли пользователь с таким id
        if (userStorage.getUserById(review.getUserId()).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }

        //Проверяем, есть ли фильм с таким id
        if (filmStorage.getFilmById(review.getFilmId()).isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }

        Review addedReview = reviewStorage.addReview(review);

        eventStorage.createEvent(addedReview.getUserId(), Event.EventType.REVIEW, Event.Operation.ADD, addedReview.getReviewId());

        return addedReview;
    }

    public Review updateReview(Review review) {

        Review oldReview = reviewStorage.getReviewById(review.getReviewId());

        Review updatedReview = reviewStorage.updateReview(review);

        // id фильма и пользователя не меняются при обновлении
        updatedReview.setUserId(oldReview.getUserId());
        updatedReview.setFilmId(oldReview.getFilmId());
        updatedReview.setUseful(0L);

        eventStorage.createEvent(updatedReview.getUserId(), Event.EventType.REVIEW, Event.Operation.UPDATE, updatedReview.getReviewId());

        return updatedReview;
    }

    public void deleteReview(Long id) {
        Review review = getReviewById(id);

        reviewStorage.deleteReview(id);

        eventStorage.createEvent(review.getUserId(), Event.EventType.REVIEW, Event.Operation.REMOVE, id);

    }

    public Review getReviewById(Long id) {
        return reviewStorage.getReviewById(id);
    }

    public List<Review> getAllReviews(Long filmId, Long count) {
        return reviewStorage.getAllReviews(filmId, count);
    }

    public List<Review> getAllReviews(Long filmId) {
        return reviewStorage.getAllReviews(filmId);
    }

    public List<Review> getAllReviews() {
        return reviewStorage.getAllReviews();
    }
}
