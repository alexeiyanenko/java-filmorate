package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import jakarta.validation.constraints.NotBlank;

@Data
public class Genre {

    @NonNull
    private final Long id;
    @NotBlank(message = "Ошибка! Название жанра не может быть пустым.")
    private final String name;
}
