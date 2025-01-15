package ru.yandex.practicum.filmorate.storage;

public interface GradeStorage {
    public void addLikeToReview(Long id, Long userId);

    public void addDislikeToReview(Long id, Long userId);

    public void deleteLikeFromReview(Long id, Long userId);

    public void deleteDislikeFromReview(Long id, Long userId);
}
