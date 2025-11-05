package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserDtoTest {

    @Test
    public void testUserDtoCreation() {
        UserDto userDto = new UserDto(1L, "John Doe", "john@example.com");

        assertEquals(1L, userDto.getId());
        assertEquals("John Doe", userDto.getName());
        assertEquals("john@example.com", userDto.getEmail());
    }

    @Test
    public void testUserDtoSetters() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");
        userDto.setEmail("john@example.com");

        assertEquals(1L, userDto.getId());
        assertEquals("John Doe", userDto.getName());
        assertEquals("john@example.com", userDto.getEmail());
    }
}