package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    public Review addReview(Review review);

    public Review updateReview(Review review);

    public void deleteReview(Long id);

    public Review getReviewById(Long id);

    public List<Review> getAllReviews(Long filmId, Long count);

    public List<Review> getAllReviews(Long filmId);

    public List<Review> getAllReviews();
}
