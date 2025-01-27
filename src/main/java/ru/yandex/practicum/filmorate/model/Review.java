package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {

    private Long reviewId;

    @NotBlank(message = "Содержание отзыва не может быть пустым.")
    private String content;

    @NotNull(message = "Оценка отзыва не может быть пустой.")
    private Boolean isPositive;

    @NotNull(message = "Должен быть указан пользователь, который добавил отзыв")
    private Long userId;

    @NotNull(message = "Должен быть указан фильм, к которому добавили отзыв")
    private Long filmId;

    private Long useful;
}
