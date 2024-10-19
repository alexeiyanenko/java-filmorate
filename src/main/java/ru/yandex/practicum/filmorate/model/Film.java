package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.yandex.practicum.filmorate.validation.ValidReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private int id;

    @NotBlank(message = "Название фильма не может быть пустым.")
    private String name;

    private Set<Genre> genres = new HashSet<>();

    private MPA rating;

    @NotNull(message = "Описание не может быть пустым.")
    @Size(max = 200, message = "Максимальная длина описания — 200 символов.")
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой.")
    @ValidReleaseDate
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом.")
    private int duration;

    private final Set<Integer> likes = new HashSet<>();

    public void addLike(int userId) {
        likes.add(userId);
    }

    public void removeLike(int userId) {
        likes.remove(userId);
    }

    public int getLikesCount() {
        return likes.size();
    }

    public enum Genre {
        COMEDY,
        DRAMA,
        ANIMATION,
        THRILLER,
        DOCUMENTARY,
        ACTION
    }

    public enum MPA {
        G,       // нет возрастных ограничений
        PG,      // детям рекомендуется смотреть с родителями
        PG_13,   // не рекомендуется детям до 13 лет
        R,       // до 17 лет в присутствии взрослого
        NC_17    // просмотр запрещён до 18 лет
    }
}
