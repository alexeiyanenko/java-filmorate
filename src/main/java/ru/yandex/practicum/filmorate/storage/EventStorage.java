package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {

    void createEvent(Long userId, Event.EventType eventType, Event.Operation operation, Long entityId);

    List<Event> getAllEventsById(long id);

}
