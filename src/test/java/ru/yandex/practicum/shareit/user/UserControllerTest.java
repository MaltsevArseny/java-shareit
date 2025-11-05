package ru.yandex.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto testUserDto;
    private UserDto createdUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto(null, "John Doe", "john@example.com");
        createdUserDto = new UserDto(1L, "John Doe", "john@example.com");
    }

    @Test
    void createUser_ShouldCallServiceAndReturnUser() throws Exception {
        // Arrange
        when(userService.createUser(any(UserDto.class))).thenReturn(createdUserDto);

        // Act & Assert
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        // Verify that service method was called
        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void updateUser_ShouldCallServiceAndReturnUpdatedUser() throws Exception {
        // Arrange
        UserDto updateDto = new UserDto(null, "John Updated", "john.updated@example.com");
        UserDto updatedUserDto = new UserDto(1L, "John Updated", "john.updated@example.com");

        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(updatedUserDto);

        // Act & Assert
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        // Verify that service method was called
        verify(userService, times(1)).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    void getUserById_ShouldCallServiceAndReturnUser() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(createdUserDto);

        // Act & Assert
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        // Verify that service method was called
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getAllUsers_ShouldCallServiceAndReturnUsersList() throws Exception {
        // Arrange
        List<UserDto> users = List.of(
                new UserDto(1L, "John Doe", "john@example.com"),
                new UserDto(2L, "Jane Smith", "jane@example.com")
        );

        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));

        // Verify that service method was called
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void deleteUser_ShouldCallService() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        // Verify that service method was called
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void getUserById_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/users/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter type"))
                .andExpect(jsonPath("$.message").exists());

        // Verify that service method was NOT called
        verify(userService, never()).getUserById(anyLong());
    }

    @Test
    void updateUser_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/users/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter type"))
                .andExpect(jsonPath("$.message").exists());

        // Verify that service method was NOT called
        verify(userService, never()).updateUser(anyLong(), any(UserDto.class));
    }

    @Test
    void deleteUser_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/users/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter type"))
                .andExpect(jsonPath("$.message").exists());

        // Verify that service method was NOT called
        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    void createUser_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(userService.createUser(any(UserDto.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"))
                .andExpect(jsonPath("$.message").value("Database error"));

        // Verify that service method was called
        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void updateUser_PartialUpdate_ShouldCallServiceWithPartialData() throws Exception {
        // Arrange
        String partialUpdateJson = "{\"name\": \"John Updated\"}";
        UserDto updatedUserDto = new UserDto(1L, "John Updated", "john@example.com");

        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(updatedUserDto);

        // Act & Assert
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partialUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        // Verify that service method was called
        verify(userService, times(1)).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify that service method was called
        verify(userService, times(1)).getAllUsers();
    }
}