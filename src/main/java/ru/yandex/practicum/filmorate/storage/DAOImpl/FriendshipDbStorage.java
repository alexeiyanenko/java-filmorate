package ru.yandex.practicum.filmorate.storage.DAOImpl;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Primary
@Repository
@Slf4j
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbcTemplate;

    public boolean addFriendship(User sender, User receiver) {
        long senderId = sender.getId();
        long receiverId = receiver.getId();

        try {
            // Проверка на существование записи в текущем направлении
            String exactCheckQuery = "SELECT COUNT(*) FROM friends " +
                    "WHERE sender_id = ? AND receiver_id = ?";
            Integer exactCount = jdbcTemplate.queryForObject(exactCheckQuery, Integer.class, senderId, receiverId);

            if (exactCount != null && exactCount > 0) {
                log.info("Дружба между пользователями {} и {} уже существует в этом направлении.", senderId, receiverId);
                return false;
            }

            // Проверка на существование дружбы в обратном направлении (receiver -> sender)
            String reverseCheckQuery = "SELECT COUNT(*) FROM friends " +
                    "WHERE sender_id = ? AND receiver_id = ?";
            Integer reverseCount = jdbcTemplate.queryForObject(reverseCheckQuery, Integer.class, receiverId, senderId);

            if (reverseCount != null && reverseCount > 0) {
                // Если существует запись в обратном направлении, добавляем новую запись и обновляем статус обеих строк
                String updateQuery = "UPDATE friends " +
                        "SET status = 'CONFIRMED' " +
                        "WHERE (sender_id = ? AND receiver_id = ?) " +
                        "OR (sender_id = ? AND receiver_id = ?)";
                jdbcTemplate.update(updateQuery, senderId, receiverId, receiverId, senderId);

                String insertQuery = "INSERT INTO friends (sender_id, receiver_id, status) VALUES (?, ?, 'CONFIRMED')";
                jdbcTemplate.update(insertQuery, senderId, receiverId);

                log.info("Дружба между {} и {} подтверждена, обе строки обновлены или добавлены.", senderId, receiverId);
                return true;
            }

            // Если дружба в обратном направлении не найдена, добавляем строку со статусом UNCONFIRMED
            String insertQuery = "INSERT INTO friends (sender_id, receiver_id, status) VALUES (?, ?, 'UNCONFIRMED')";
            jdbcTemplate.update(insertQuery, senderId, receiverId);
            log.info("Дружба между пользователями {} и {} добавлена со статусом UNCONFIRMED.", senderId, receiverId);

            return true;

        } catch (Exception e) {
            log.error("Ошибка при добавлении дружбы между {} и {}: {}", senderId, receiverId, e.getMessage());
            throw new RuntimeException("Не удалось добавить дружбу", e);
        }
    }

    public boolean deleteFriendship(User sender, User receiver) {
        long senderId = sender.getId();
        long receiverId = receiver.getId();

        try {
            // Удаляем запись в текущем направлении (sender -> receiver)
            String deleteQuery = "DELETE FROM friends WHERE sender_id = ? AND receiver_id = ?";
            int rowsDeleted = jdbcTemplate.update(deleteQuery, senderId, receiverId);

            // Проверяем наличие строки в обратном направлении (receiver -> sender)
            String checkReverseQuery = "SELECT COUNT(*) FROM friends WHERE sender_id = ? AND receiver_id = ?";
            Integer reverseCount = jdbcTemplate.queryForObject(checkReverseQuery, Integer.class, receiverId, senderId);

            if (reverseCount != null && reverseCount > 0) {
                // Обновляем статус строки в обратном направлении на UNCONFIRMED
                String updateReverseQuery = "UPDATE friends SET status = 'UNCONFIRMED' WHERE sender_id = ? AND receiver_id = ?";
                jdbcTemplate.update(updateReverseQuery, receiverId, senderId);
            }

            return rowsDeleted > 0;

        } catch (Exception e) {
            log.error("Ошибка при удалении дружбы между {} и {}: {}", senderId, receiverId, e.getMessage());
            throw new RuntimeException("Не удалось удалить дружбу", e);
        }
    }

    public List<Long> getFriendships(Long userId) {
        String sqlQuery = "select receiver_id from friends " +
                "where sender_id = ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> rs.getLong("receiver_id"), userId);
    }

}
