package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import jakarta.validation.constraints.NotBlank;

@Data
public class MPA {
    // МРА определяет возрастное ограничение для фильма:
    // G - у фильма нет возрастных ограничений,
    // PG - детям рекомендуется смотреть фильм с родителями,
    // PG-13 - детям до 13 лет просмотр не желателен,
    // R - лицам до 17 лет просматривать фильм можно только в присутствии взрослого,
    // NC-17 - лицам до 18 лет просмотр запрещён.
    @NonNull
    private final Long id;
    @NotBlank(message = "Ошибка! Рейтинг не может быть пустым.")
    private final String name;
    @NotBlank(message = "Ошибка! Описание не может быть пустым.")
    private final String description;
}
