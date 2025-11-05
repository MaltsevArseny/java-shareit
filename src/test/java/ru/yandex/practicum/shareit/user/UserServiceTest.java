package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
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
    void createUser_ShouldSaveAndReturnUserDto() {
        // Arrange
        UserDto inputDto = new UserDto(null, "John Doe", "john@example.com");
        User savedUser = new User(1L, "John Doe", "john@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserDto result = userService.createUser(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUserDto() {
        // Arrange
        User existingUser = new User(1L, "John Doe", "john@example.com");
        UserDto updateDto = new UserDto(null, "John Updated", null);
        User updatedUser = new User(1L, "John Updated", "john@example.com");

        when(userRepository.findById(1L)).thenReturn(existingUser);
        when(userRepository.update(any(User.class))).thenReturn(updatedUser);

        // Act
        UserDto result = userService.updateUser(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).update(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUserDto() {
        // Arrange
        User user = new User(1L, "John Doe", "john@example.com");
        when(userRepository.findById(1L)).thenReturn(user);

        // Act
        UserDto result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUserDtos() {
        // Arrange
        List<User> users = List.of(
                new User(1L, "John Doe", "john@example.com"),
                new User(2L, "Jane Smith", "jane@example.com")
        );
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserDto> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void deleteUser_ShouldCallRepository() {
        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateUser_WithNonExistentUser_ShouldReturnNull() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(null);

        // Act
        UserDto result = userService.updateUser(999L, new UserDto());

        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).update(any(User.class));
    }
}