package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    @Test
    public void testToUserDto() {
        User user = new User(1L, "John Doe", "john@example.com");
        UserDto userDto = UserMapper.toUserDto(user);

        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    public void testToUser() {
        UserDto userDto = new UserDto(1L, "John Doe", "john@example.com");
        User user = UserMapper.toUser(userDto);

        assertEquals(userDto.getId(), user.getId());
        assertEquals(userDto.getName(), user.getName());
        assertEquals(userDto.getEmail(), user.getEmail());
    }

    @Test
    public void testToUserDtoWithNull() {
        assertNull(UserMapper.toUserDto(null));
    }

    @Test
    public void testToUserWithNull() {
        assertNull(UserMapper.toUser(null));
    }
}