package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.yandex.practicum.filmorate.validation.ValidReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым.")
    private String name;

    private Set<Genre> genres = new LinkedHashSet<>();

    @JsonProperty("mpa")
    private MPA MPARating;

    @NotNull(message = "Описание не может быть пустым.")
    @Size(max = 200, message = "Максимальная длина описания — 200 символов.")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой.")
    @ValidReleaseDate
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом.")
    private Long duration;

    private final Set<Long> likes = new HashSet<>();

    public void setGenres(Set<Genre> genres) {
        this.genres.clear();
        this.genres.addAll(genres);
    }

    public void setLikes(Set<Long> likes) {
        this.likes.clear();
        this.likes.addAll(likes);
    }

    public int getLikesCount() {
        return likes.size();
    }
}
