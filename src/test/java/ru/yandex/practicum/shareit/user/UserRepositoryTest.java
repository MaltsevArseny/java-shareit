package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;
    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();

        testUser1 = new User(null, "John Doe", "john@example.com");
        testUser2 = new User(null, "Jane Smith", "jane@example.com");
    }

    @Test
    void save_ShouldAssignIdAndSaveUser() {
        // Act
        User savedUser = userRepository.save(testUser1);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john@example.com", savedUser.getEmail());
    }

    @Test
    void save_WithExistingId_ShouldUpdateUser() {
        // Arrange
        User savedUser = userRepository.save(testUser1);
        Long userId = savedUser.getId();

        // Act - update the user
        savedUser.setName("John Updated");
        User updatedUser = userRepository.save(savedUser);

        // Assert
        assertEquals(userId, updatedUser.getId());
        assertEquals("John Updated", updatedUser.getName());
    }

    @Test
    void findById_ShouldReturnUser() {
        // Arrange
        User savedUser = userRepository.save(testUser1);
        Long userId = savedUser.getId();

        // Act
        User foundUser = userRepository.findById(userId);

        // Assert
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        assertEquals("John Doe", foundUser.getName());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnNull() {
        // Act
        User foundUser = userRepository.findById(999L);

        // Assert
        assertNull(foundUser);
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        userRepository.save(testUser1);
        userRepository.save(testUser2);

        // Act
        List<User> allUsers = userRepository.findAll();

        // Assert
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(user -> user.getName().equals("John Doe")));
        assertTrue(allUsers.stream().anyMatch(user -> user.getName().equals("Jane Smith")));
    }

    @Test
    void findAll_WithEmptyRepository_ShouldReturnEmptyList() {
        // Act
        List<User> allUsers = userRepository.findAll();

        // Assert
        assertNotNull(allUsers);
        assertTrue(allUsers.isEmpty());
    }

    @Test
    void update_ShouldUpdateExistingUser() {
        // Arrange
        User savedUser = userRepository.save(testUser1);
        Long userId = savedUser.getId();

        // Act - update the user
        savedUser.setName("John Updated");
        savedUser.setEmail("john.updated@example.com");
        User updatedUser = userRepository.update(savedUser);

        // Assert
        assertNotNull(updatedUser);
        assertEquals(userId, updatedUser.getId());
        assertEquals("John Updated", updatedUser.getName());
        assertEquals("john.updated@example.com", updatedUser.getEmail());
    }

    @Test
    void update_WithNonExistentUser_ShouldReturnNull() {
        // Arrange
        User nonExistentUser = new User(999L, "Non-existent", "none@example.com");

        // Act
        User result = userRepository.update(nonExistentUser);

        // Assert
        assertNull(result);
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        // Arrange
        User savedUser = userRepository.save(testUser1);
        Long userId = savedUser.getId();

        // Verify user exists before deletion
        assertNotNull(userRepository.findById(userId));

        // Act
        userRepository.deleteById(userId);

        // Assert
        assertNull(userRepository.findById(userId));
    }

    @Test
    void deleteById_WithNonExistentId_ShouldDoNothing() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> userRepository.deleteById(999L));
    }

    @Test
    void idCounter_ShouldIncrementForNewUsers() {
        // Act
        User user1 = userRepository.save(testUser1);
        User user2 = userRepository.save(testUser2);

        // Assert
        assertEquals(1L, user1.getId());
        assertEquals(2L, user2.getId());
    }
}