package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    @Qualifier("eventDbStorage")
    private final EventStorage eventStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public List<Event> getAllEventsById(long id) {
        if (userStorage.getUserById(id).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }

        return eventStorage.getAllEventsById(id);
    }
}
