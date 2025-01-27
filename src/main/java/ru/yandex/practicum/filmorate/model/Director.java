package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Director {
    @NonNull
    private Long id;
    @NotNull
    @NotBlank(message = "Ошибка! Имя режиссера не может быть пустым.")
    private String name;
}
