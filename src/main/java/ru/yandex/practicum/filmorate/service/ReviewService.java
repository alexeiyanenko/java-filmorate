package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.DAOImpl.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.DAOImpl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.DAOImpl.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.DAOImpl.UserDbStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewDbStorage reviewDbStorage;
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final EventDbStorage eventDbStorage;

    public Review addReview(Review review) {

        //Проверяем, есть ли пользователь с таким id
        if (userDbStorage.getUserById(review.getUserId()).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }

        //Проверяем, есть ли фильм с таким id
        if (filmDbStorage.getFilmById(review.getFilmId()).isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }

        Review addedReview = reviewDbStorage.addReview(review);

        eventDbStorage.createEvent(addedReview.getUserId(), Event.EventType.REVIEW, Event.Operation.ADD, addedReview.getReviewId());

        return addedReview;
    }

    public Review updateReview(Review review) {

        Review updatedReview = reviewDbStorage.updateReview(review);

        eventDbStorage.createEvent(updatedReview.getUserId(), Event.EventType.REVIEW, Event.Operation.UPDATE, updatedReview.getReviewId());

        return updatedReview;
    }

    public void deleteReview(Long id) {
        Review review = getReviewById(id);

        reviewDbStorage.deleteReview(id);

        eventDbStorage.createEvent(review.getUserId(), Event.EventType.REVIEW, Event.Operation.REMOVE, id);

    }

    public Review getReviewById(Long id) {
        return reviewDbStorage.getReviewById(id);
    }

    public List<Review> getAllReviews(Long filmId, Long count) {
        return reviewDbStorage.getAllReviews(filmId, count);
    }

    public List<Review> getAllReviews(Long filmId) {
        return reviewDbStorage.getAllReviews(filmId);
    }

    public List<Review> getAllReviews() {
        return reviewDbStorage.getAllReviews();
    }
}
