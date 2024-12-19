package ru.yandex.practicum.filmorate.storage;

public interface EventStorage {

    void createEvent(Long userId, String eventType, String operation, Long entityId);

}
