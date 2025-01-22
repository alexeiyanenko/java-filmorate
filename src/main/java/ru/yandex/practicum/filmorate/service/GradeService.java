package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Grade;
import ru.yandex.practicum.filmorate.storage.GradeStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GradeService {

    @Qualifier("gradeDbStorage")
    private final GradeStorage gradeStorage;

    public void addLikeToReview(Long id, Long userId) {

        // Проверяем, есть ли уже такая запись в БД с оценкой LIKE
        if (isGradeExists(id, userId, "LIKE")) {
            log.info("Лайк от пользователя {} к отзыву {} уже добавлен", userId, id);
            return;
        }

        // Проверяем, есть ли уже такая запись в БД с оценкой DISLIKE
        if (isGradeExists(id, userId, "DISLIKE")) {
            log.info("Есть дизлайк от пользователя {} к отзыву {}", userId, id);
            deleteDislikeFromReview(id, userId);
        }

        //Увеличиваем рейтинг отзыва на 1
        gradeStorage.addRatingToUseful(id);

        gradeStorage.addLikeToReview(id, userId);
    }

    public void addDislikeToReview(Long id, Long userId) {
        //Проверяем, есть ли уже такая запись в БД с оценкой DISLIKE
        if (isGradeExists(id, userId, "DISLIKE")) {

            log.info("Дизлайк от пользователя {} к отзыву {} уже добавлен", userId, id);

            return;
        }

        //Проверяем, есть ли уже такая запись в БД с оценкой LIKE
        if (isGradeExists(id, userId, "LIKE")) {
            log.info("Есть лайк от пользователя {} к отзыву {}", userId, id);
            deleteLikeFromReview(id, userId);
        }

        //Уменьшаем рейтинг отзыва на 1
        gradeStorage.decreaseRatingToUseful(id);

        gradeStorage.addDislikeToReview(id, userId);
    }

    public void deleteLikeFromReview(Long id, Long userId) {
        //Уменьшаем рейтинг отзыва на 1, так как лайк будет удалён
        gradeStorage.decreaseRatingToUseful(id);

        gradeStorage.deleteLikeFromReview(id, userId);
    }

    public void deleteDislikeFromReview(Long id, Long userId) {
        //Увеличиваем рейтинг отзыва на 1, так как дизлайк будет удалён
        gradeStorage.addRatingToUseful(id);

        gradeStorage.deleteDislikeFromReview(id, userId);
    }

    public boolean isGradeExists(Long id, Long userId, String grade) {
        List<Grade> grades = gradeStorage.getAllGrades();

        return grades.stream()
                .anyMatch(g -> g.getUserId().equals(userId) && g.getGrade().equals(grade) && g.getReviewId().equals(id));
    }
}
