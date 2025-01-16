package ru.yandex.practicum.filmorate.storage.DAOImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Primary
@Repository
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    @Override
    public Review addReview(Review review) {
        String sqlQuery = "INSERT INTO reviews (content, isPositive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        //Проверяем, есть ли пользователь с таким id
        if (userDbStorage.getUserById(review.getUserId()).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }

        //Проверяем, есть ли фильм с таким id
        if (filmDbStorage.getFilmById(review.getFilmId()).isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"review_id"});
                stmt.setString(1, review.getContent());
                stmt.setBoolean(2, review.getIsPositive());
                stmt.setLong(3, review.getUserId());
                stmt.setLong(4, review.getFilmId());
                stmt.setLong(5, 0);
                return stmt;
            }, keyHolder);
        } catch (Exception e) {
            log.error("Ошибка при создании отзыва: {}", e.getMessage(), e);
            throw new DataAccessException("Ошибка при добавлении отзыва в БД", e) {
            };
        }

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        review.setUseful(0L);

        log.info("Создали отзыв: {}", review);

        return review;
    }

    @Override
    public Review updateReview(Review review) {

        //Если значение useful не указано, то присваиваем 0
        if (review.getUseful() == null) {
            review.setUseful(0L);
        }

        String sqlQuery = "UPDATE reviews SET content = ?, isPositive = ?, user_id = ?, film_id = ?, useful = ?" +
                " WHERE review_id = ?";
        int rowsUpdated = jdbcTemplate.update(sqlQuery,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful(),
                review.getReviewId());

        if (rowsUpdated == 0) {
            throw new NoSuchElementException("Отзыв не найден: " + review.getReviewId());
        }

        log.info("Обновили отзыв: {}", review);

        return review;
    }

    @Override
    public void deleteReview(Long id) {
        String sqlQuery = "DELETE FROM reviews WHERE review_id = ?";
        int rowsDeleted = jdbcTemplate.update(sqlQuery, id);

        if (rowsDeleted == 0) {
            throw new NoSuchElementException("Отзыв с ID " + id + " не найден.");
        }

        log.info("Удалили отзыв: {}", id);
    }

    @Override
    public Review getReviewById(Long id) {

        String sqlQuery = "SELECT * FROM reviews WHERE review_id = ?";

        Optional<Review> review = Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, this::mapRowToReview, id));

        if (review.isPresent()) {
            log.info("Получили отзыв с id: {}", id);
            return review.get();
        }

        throw new NotFoundException("Отзыв с таким ID не найден");
    }

    @Override
    public List<Review> getAllReviews(Long filmId, Long count) {

        //Если указан фильм и количество отзывов

        String sqlQuery = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";

        log.info("Получили все отзывы для фильма {} с количеством записей {}", filmId, count);

        return jdbcTemplate.query(sqlQuery, this::mapRowToReview, filmId, count);
    }

    public List<Review> getAllReviews(Long filmId) {

        //Если указан фильм и не указано количество отзывов

        Long count = 10L;
        String sqlQuery = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";

        log.info("Получили все отзывы для фильма {}", filmId);

        return jdbcTemplate.query(sqlQuery, this::mapRowToReview, filmId, count);
    }

    public List<Review> getAllReviews() {

        //Если не указано ничего, то выводим все отзывы c лимитом 10

        Long count = 10L;
        String sqlQuery = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";

        log.info("Получили все отзывы");

        return jdbcTemplate.query(sqlQuery, this::mapRowToReview, count);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("isPositive"));
        review.setUserId(rs.getLong("user_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUseful(rs.getLong("useful"));
        return review;
    }
}
