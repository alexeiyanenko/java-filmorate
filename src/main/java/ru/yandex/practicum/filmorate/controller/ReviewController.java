package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.GradeService;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
@Validated
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final GradeService gradeService;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Создание отзыва: {}", review);
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Обновление отзыва: {}", review);
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        log.info("Удаление отзыва: {}", id);
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        log.info("Запрос отзыва с id: {}", id);
        return reviewService.getReviewById(id);
    }

    @GetMapping(params = {"filmId", "count"})
    public List<Review> getAllReviews(@RequestParam Long filmId, @RequestParam Long count) {
        log.info("Запрос на получение всех отзывов, если указан фильм и количество отзывов");
        return reviewService.getAllReviews(filmId, count);
    }

    @GetMapping(params = "filmId")
    public List<Review> getAllReviews(@RequestParam Long filmId) {
        log.info("Запрос на получение всех отзывов, если не указано количество");
        return reviewService.getAllReviews(filmId);
    }

    @GetMapping
    public List<Review> getAllReviews() {
        log.info("Запрос на получение всех отзывов, если не указан фильм и количество отзывов");
        return reviewService.getAllReviews();
    }

    @PutMapping("{id}/like/{userId}")
    public void addLikeToReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление лайка в отзыв от пользователя: {} -> {}", id, userId);
        gradeService.addLikeToReview(id, userId);
    }

    @PutMapping("{id}/dislike/{userId}")
    public void addDislikeToReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Добавление дизлайка в отзыв от пользователя: {} -> {}", id, userId);
        gradeService.addDislikeToReview(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void deleteLikeFromReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка из отзыва от пользователя: {} -> {}", id, userId);
        gradeService.deleteLikeFromReview(id, userId);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public void deleteDislikeFromReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление дизлайка из отзыва от пользователя: {} -> {}", id, userId);
        gradeService.deleteDislikeFromReview(id, userId);
    }
}
