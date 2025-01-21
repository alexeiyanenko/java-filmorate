package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Grade;
import ru.yandex.practicum.filmorate.storage.DAOImpl.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.DAOImpl.GradeDbStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GradeService {

    private final GradeDbStorage gradeDbStorage;
    private final EventDbStorage eventDbStorage;

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
        gradeDbStorage.addRatingToUseful(id);

        gradeDbStorage.addLikeToReview(id, userId);

        eventDbStorage.createEvent(userId, Event.EventType.LIKE, Event.Operation.ADD, id);
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
        gradeDbStorage.decreaseRatingToUseful(id);

        gradeDbStorage.addDislikeToReview(id, userId);

        eventDbStorage.createEvent(userId, Event.EventType.DISLIKE, Event.Operation.ADD, id);
    }

    public void deleteLikeFromReview(Long id, Long userId) {
        //Уменьшаем рейтинг отзыва на 1, так как лайк будет удалён
        gradeDbStorage.decreaseRatingToUseful(id);

        gradeDbStorage.deleteLikeFromReview(id, userId);

        eventDbStorage.createEvent(userId, Event.EventType.LIKE, Event.Operation.REMOVE, id);
    }

    public void deleteDislikeFromReview(Long id, Long userId) {
        //Увеличиваем рейтинг отзыва на 1, так как дизлайк будет удалён
        gradeDbStorage.addRatingToUseful(id);

        gradeDbStorage.deleteDislikeFromReview(id, userId);

        eventDbStorage.createEvent(userId, Event.EventType.DISLIKE, Event.Operation.REMOVE, id);
    }

    public boolean isGradeExists(Long id, Long userId, String grade) {
        List<Grade> grades = gradeDbStorage.getAllGrades();

        return grades.stream()
                .anyMatch(g -> g.getUserId().equals(userId) && g.getGrade().equals(grade) && g.getReviewId().equals(id));
    }
}
