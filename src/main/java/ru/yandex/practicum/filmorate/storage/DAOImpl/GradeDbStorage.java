package ru.yandex.practicum.filmorate.storage.DAOImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Grade;
import ru.yandex.practicum.filmorate.storage.GradeStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

@Primary
@Repository
@Slf4j
@RequiredArgsConstructor
public class GradeDbStorage implements GradeStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
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
        addRatingToUseful(id);

        String sqlQuery = "INSERT INTO grades (user_id, review_id, grade) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
                stmt.setLong(1, userId);
                stmt.setLong(2, id);
                stmt.setString(3, "LIKE");
                return stmt;
            }, keyHolder);

            log.info("Добавили лайк в отзыв от пользователя: {} -> {}", id, userId);

        } catch (Exception e) {
            log.error("Ошибка при добавлении лайка к отзыву: {}", e.getMessage(), e);
            throw new DataAccessException("Ошибка при добавлении лайка к отзыву в БД", e) {
            };

        }

    }

    @Override
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
        decreaseRatingToUseful(id);

        String sqlQuery = "INSERT INTO grades (user_id, review_id, grade) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
                stmt.setLong(1, userId);
                stmt.setLong(2, id);
                stmt.setString(3, "DISLIKE");
                return stmt;
            }, keyHolder);

            log.info("Добавили дизлайк в отзыв от пользователя: {} -> {}", id, userId);

        } catch (Exception e) {
            log.error("Ошибка при добавлении дизлайка к отзыву: {}", e.getMessage(), e);
            throw new DataAccessException("Ошибка при добавлении дизлайка к отзыву в БД", e) {
            };
        }
    }

    @Override
    public void deleteLikeFromReview(Long id, Long userId) {

        //Уменьшаем рейтинг отзыва на 1, так как лайк будет удалён
        decreaseRatingToUseful(id);

        String sqlQuery = "DELETE FROM grades WHERE review_id = ? AND user_id = ?";
        int rowsDeleted = jdbcTemplate.update(sqlQuery, id, userId);
        if (rowsDeleted == 0) {
            throw new NoSuchElementException("Ошибка при удалении лайка");
        }

        log.info("Удалили лайк из отзыва от пользователя: {} -> {}", id, userId);
    }

    @Override
    public void deleteDislikeFromReview(Long id, Long userId) {

        //Увеличиваем рейтинг отзыва на 1, так как дизлайк будет удалён
        addRatingToUseful(id);

        String sqlQuery = "DELETE FROM grades WHERE review_id = ? AND user_id = ?";
        int rowsDeleted = jdbcTemplate.update(sqlQuery, id, userId);
        if (rowsDeleted == 0) {
            throw new NoSuchElementException("Ошибка при удалении дизлайка");
        }

        log.info("Удалили дизлайк из отзыва от пользователя: {} -> {}", id, userId);
    }

    public void addRatingToUseful(Long id) {
        String sqlQueryForUseful = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
        jdbcTemplate.update(sqlQueryForUseful, id);
    }

    public void decreaseRatingToUseful(Long id) {
        String sqlQueryForUseful = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
        jdbcTemplate.update(sqlQueryForUseful, id);
    }

    public boolean isGradeExists(Long id, Long userId, String grade) {
        List<Grade> grades = getAllGrades();

        return grades.stream()
            .anyMatch(g -> g.getUserId().equals(userId) && g.getGrade().equals(grade) && g.getReviewId().equals(id));
    }

    public List<Grade> getAllGrades() {

        String sqlQuery = "SELECT * FROM grades";

        log.info("Получили все оценки");

        return jdbcTemplate.query(sqlQuery, this::mapRowToReview);
    }

    private Grade mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Grade grade = new Grade();

        grade.setGradeId(rs.getLong("grade_id"));
        grade.setUserId(rs.getLong("user_id"));
        grade.setReviewId(rs.getLong("review_id"));
        grade.setGrade(rs.getString("grade"));
        return grade;
    }
}
