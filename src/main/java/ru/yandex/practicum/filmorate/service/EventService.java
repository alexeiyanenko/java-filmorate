package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.DAOImpl.EventDbStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final EventDbStorage eventDbStorage;

    public List<Event> getAllEventsById(long id) {
        return eventDbStorage.getAllEventsById(id);
    }
}
