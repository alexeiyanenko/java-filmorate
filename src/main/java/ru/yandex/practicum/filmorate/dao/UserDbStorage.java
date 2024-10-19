package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO users (name, email, login, birthday) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());
        log.info("Пользователь добавлен: {}", user);
        return user;
    }

    @Override
    public User updateUser(User updatedUser) {
        String sql = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE user_id = ?";
        if (jdbcTemplate.update(sql, updatedUser.getName(), updatedUser.getEmail(),
                updatedUser.getLogin(), updatedUser.getBirthday(), updatedUser.getId()) == 0) {
            throw new NotFoundException("Пользователь с ID " + updatedUser.getId() + " не найден.");
        }
        log.info("Пользователь обновлён: {}", updatedUser);
        return updatedUser;
    }

    @Override
    public Optional<User> getById(Integer id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, id).stream().findFirst();
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User(); // Assuming default constructor
        user.setId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }

    @Override
    public Set<Integer> getFriendsIDs(int id) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        List<Integer> friendsIds = jdbcTemplate.queryForList(sql, Integer.class, id);

        if (friendsIds.isEmpty()) {
            log.info("Пользователь с ID {} не имеет друзей.", id);
        } else {
            log.info("Друзья пользователя с ID {}: {}", id, friendsIds);
        }

        return new HashSet<>(friendsIds);
    }

    @Override
    public boolean isUserExist(int id) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }
}
