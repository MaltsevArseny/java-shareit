package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserControllerIntegrationTest {

    @Autowired
    private UserController userController;

    @MockBean
    private UserService userService;

    @Test
    void createUser_ShouldUseService() {
        // Arrange
        UserDto inputDto = new UserDto(null, "John Doe", "john@example.com");
        UserDto expectedDto = new UserDto(1L, "John Doe", "john@example.com");

        when(userService.createUser(any(UserDto.class))).thenReturn(expectedDto);

        // Act
        UserDto result = userController.createUser(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(userService, times(1)).createUser(inputDto);
    }

    @Test
    void updateUser_ShouldUseService() {
        // Arrange
        UserDto updateDto = new UserDto(null, "John Updated", "john.updated@example.com");
        UserDto expectedDto = new UserDto(1L, "John Updated", "john.updated@example.com");

        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(expectedDto);

        // Act
        UserDto result = userController.updateUser(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        assertEquals("john.updated@example.com", result.getEmail());
        verify(userService, times(1)).updateUser(1L, updateDto);
    }

    @Test
    void getUserById_ShouldUseService() {
        // Arrange
        UserDto expectedDto = new UserDto(1L, "John Doe", "john@example.com");
        when(userService.getUserById(1L)).thenReturn(expectedDto);

        // Act
        UserDto result = userController.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getAllUsers_ShouldUseService() {
        // Arrange
        List<UserDto> expectedUsers = List.of(
                new UserDto(1L, "John Doe", "john@example.com"),
                new UserDto(2L, "Jane Smith", "jane@example.com")
        );

        when(userService.getAllUsers()).thenReturn(expectedUsers);

        // Act
        List<UserDto> result = userController.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void deleteUser_ShouldUseService() {
        // Act
        userController.deleteUser(1L);

        // Assert
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void updateUser_PartialUpdate_ShouldHandleNullFields() {
        // Arrange - only name is provided, email should remain unchanged
        UserDto partialUpdate = new UserDto(null, "John Updated", null);
        UserDto expectedDto = new UserDto(1L, "John Updated", "john@example.com");

        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(expectedDto);

        // Act
        UserDto result = userController.updateUser(1L, partialUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        verify(userService, times(1)).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    void createUser_WithEmptyUser_ShouldCallService() {
        // Arrange
        UserDto emptyUser = new UserDto();
        UserDto expectedDto = new UserDto(1L, null, null);

        when(userService.createUser(any(UserDto.class))).thenReturn(expectedDto);

        // Act
        UserDto result = userController.createUser(emptyUser);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userService, times(1)).createUser(emptyUser);
    }
}