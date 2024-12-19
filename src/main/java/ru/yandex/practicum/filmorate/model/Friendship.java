package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Friendship {

    private Long senderId; // пользователь, который отправил запрос на добавление другого пользователя в друзья
    private Long receiverId; // пользователь, которому отправили запрос
    private String status;

}
