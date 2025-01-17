package ru.yandex.practicum.filmorate.storage.DAOImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Primary
@Repository
@Slf4j
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void createEvent(Long userId, Event.EventType eventType, Event.Operation operation, Long entityId) {

        Long timestamp = Instant.now().toEpochMilli();
        String sqlQuery = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlQuery, userId, timestamp, eventType.name(), operation.name(), entityId);

    }

    @Override
    public List<Event> getAllEvents(long id) {
        String sqlQuery = "SELECT * FROM events WHERE user_id = ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToEvent, id);
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getLong("event_id"));
        event.setUserId(rs.getLong("user_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        event.setEventType(Event.EventType.valueOf(rs.getString("event_type")));
        event.setOperation(Event.Operation.valueOf(rs.getString("operation")));
        event.setEntityId(rs.getLong("entity_id"));
        return event;
    }
}
