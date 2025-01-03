package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

public interface EventStorage {

    void createEvent(Long userId, Event.EventType eventType, Event.Operation operation, Long entityId);

}
