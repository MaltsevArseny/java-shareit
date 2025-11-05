package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

public class UserServiceImplTest {

    private UserRepository userRepository;
    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl();
        // Используем рефлексию для установки мок-репозитория
        try {
            var field = UserServiceImpl.class.getDeclaredField("userRepository");
            field.setAccessible(true);
            field.set(userService, userRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateUser() {
        UserDto userDto = new UserDto(null, "John Doe", "john@example.com");
        User user = new User(1L, "John Doe", "john@example.com");

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateUser() {
        User existingUser = new User(1L, "John Doe", "john@example.com");
        UserDto updateDto = new UserDto(null, "John Updated", null);
        User updatedUser = new User(1L, "John Updated", "john@example.com");

        when(userRepository.findById(1L)).thenReturn(existingUser);
        when(userRepository.update(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.updateUser(1L, updateDto);

        assertEquals("John Updated", result.getName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    public void testGetUserById() {
        User user = new User(1L, "John Doe", "john@example.com");

        when(userRepository.findById(1L)).thenReturn(user);

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testGetAllUsers() {
        User user1 = new User(1L, "John Doe", "john@example.com");
        User user2 = new User(2L, "Jane Smith", "jane@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
    }
}