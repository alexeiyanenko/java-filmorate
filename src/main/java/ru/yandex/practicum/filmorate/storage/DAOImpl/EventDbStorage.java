package ru.yandex.practicum.filmorate.storage.DAOImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.sql.Timestamp;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@Primary
@Repository
@Slf4j
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void createEvent(Long userId, Event.EventType eventType, Event.Operation operation, Long entityId) {

        long timestamp = Timestamp.from(Instant.now()).getTime();
        String sqlQuery = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlQuery, userId, timestamp, eventType.name(), operation.name(), entityId);

    }
}
