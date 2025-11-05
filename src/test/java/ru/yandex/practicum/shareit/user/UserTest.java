package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testUserCreation() {
        User user = new User(1L, "John Doe", "john@example.com");

        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    public void testUserSetters() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");

        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    public void testUserEquals() {
        User user1 = new User(1L, "John Doe", "john@example.com");
        User user2 = new User(1L, "John Doe", "john@example.com");

        assertEquals(user1.getId(), user2.getId());
    }
}