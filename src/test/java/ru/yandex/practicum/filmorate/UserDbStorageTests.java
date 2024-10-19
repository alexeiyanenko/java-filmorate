package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTests {
    private final UserDbStorage userStorage;

    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setId(1);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        userStorage.createUser(user);
    }

    @Test
    public void testFindUserById() {
        Optional<User> userOptional = userStorage.getById(1);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    public void testCreateUser() {
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("newuser@example.com");
        newUser.setLogin("newlogin");
        newUser.setBirthday(LocalDate.of(1995, 5, 15));

        User createdUser = userStorage.createUser(newUser);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isGreaterThan(1);
        assertThat(createdUser.getName()).isEqualTo("New User");
    }

    @Test
    public void testUpdateUser() {
        User userToUpdate = new User();
        userToUpdate.setId(1);
        userToUpdate.setName("Updated User");
        userToUpdate.setEmail("updated@example.com");
        userToUpdate.setLogin("updatedlogin");
        userToUpdate.setBirthday(LocalDate.of(2000, 1, 1));

        userStorage.updateUser(userToUpdate);

        Optional<User> updatedUserOptional = userStorage.getById(1);
        assertThat(updatedUserOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "Updated User")
                );
    }
}