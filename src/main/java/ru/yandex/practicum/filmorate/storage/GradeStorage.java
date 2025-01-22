package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Grade;

import java.util.List;

public interface GradeStorage {
    public void addLikeToReview(Long id, Long userId);

    public void addDislikeToReview(Long id, Long userId);

    public void deleteLikeFromReview(Long id, Long userId);

    public void deleteDislikeFromReview(Long id, Long userId);

    List<Grade> getAllGrades();

    void addRatingToUseful(Long id);

    void decreaseRatingToUseful(Long id);
}
