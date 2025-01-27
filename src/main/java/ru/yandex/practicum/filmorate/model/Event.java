package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Event {

    private Long eventId;
    @NotNull
    private Long userId;
    @NotNull
    private Long timestamp;
    @NotNull
    private EventType eventType;
    @NotNull
    private Operation operation;
    @NotNull
    private Long entityId;

    public enum EventType {
        LIKE,
        DISLIKE,
        FRIEND,
        REVIEW
    }

    public enum Operation {
        ADD,
        REMOVE,
        UPDATE
    }
}
