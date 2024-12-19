package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DAOImpl.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.DAOImpl.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FriendshipDbStorage.class, UserDbStorage.class})
class FriendshipDbStorageTests {
    private final FriendshipDbStorage friendshipStorage;
    private final UserDbStorage userStorage;

    private User user1;
    private User user2;

    @BeforeEach
    public void beforeEach() {
        user1 = new User();
        user1.setEmail("glasha@example.com");
        user1.setLogin("glasha");
        user1.setName("Глаша");
        user1.setBirthday(LocalDate.of(1995, 4, 20));

        user2 = new User();
        user2.setEmail("timofey@example.com");
        user2.setLogin("timofey");
        user2.setName("Тимофей");
        user2.setBirthday(LocalDate.of(1990, 7, 15));

        userStorage.createUser(user1);
        userStorage.createUser(user2);
    }

    @Test
    public void testAddFriendship() {
        boolean added = friendshipStorage.addFriendship(user1, user2);
        assertThat(added).isTrue();
    }

    @Test
    public void testDeleteFriendship() {
        friendshipStorage.addFriendship(user1, user2);
        boolean deleted = friendshipStorage.deleteFriendship(user1, user2);
        assertThat(deleted).isTrue();
    }

    @Test
    public void testGetFriends() {
        List<Long> friends = friendshipStorage.getFriendships(user1.getId());
        assertThat(friends).isNotNull();
    }
}
