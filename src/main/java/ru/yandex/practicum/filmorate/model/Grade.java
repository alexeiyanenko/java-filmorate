package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Grade {
    private Long gradeId;
    private Long userId;
    private Long reviewId;
    private String grade;
}
