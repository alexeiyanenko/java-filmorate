package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DAOImpl.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class UserDbStorageTests {
    private final UserDbStorage userStorage;
    private User user1;
    private User user2;

    @BeforeEach
    public void beforeEach() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("glasha@example.com");
        user1.setLogin("glasha");
        user1.setName("Глаша");
        user1.setBirthday(LocalDate.of(1995, 4, 20));

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("timofey@example.com");
        user2.setLogin("timofey");
        user2.setName("Тимофей");
        user2.setBirthday(LocalDate.of(1990, 7, 15));

        userStorage.createUser(user1);
        userStorage.createUser(user2);
    }

    @Test
    public void testCreateUser() {
        User createdUser = userStorage.createUser(user1);

        Optional<User> retrievedUser = userStorage.getUserById(createdUser.getId());
        assertThat(retrievedUser).isPresent().hasValueSatisfying(u ->
                assertThat(u).hasFieldOrPropertyWithValue("email", "glasha@example.com")
                        .hasFieldOrPropertyWithValue("login", "glasha")
        );
    }

    @Test
    public void testUpdateUser() {
        user1.setName("Updated Name");
        userStorage.updateUser(user1);

        Optional<User> updatedUser = userStorage.getUserById(user1.getId());
        assertThat(updatedUser).isPresent().hasValueSatisfying(u ->
                assertThat(u).hasFieldOrPropertyWithValue("name", "Updated Name")
        );
    }

    @Test
    public void testDeleteUser() {
        userStorage.deleteUser(user1.getId());

        Optional<User> retrievedUser = userStorage.getUserById(user1.getId());
        assertThat(retrievedUser).isEmpty();
    }

    @Test
    public void testGetUserById() {
        Optional<User> retrievedUser = userStorage.getUserById(user1.getId());

        assertThat(retrievedUser).isPresent().hasValueSatisfying(u ->
                assertThat(u).hasFieldOrPropertyWithValue("id", user1.getId())
        );
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = userStorage.getAllUsers();
        assertThat(users).hasSize(2);
    }
}