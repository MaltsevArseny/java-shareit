package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    UserRepositoryTest(TestEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Test
    void save_ShouldSaveUser() {
        User user = new User(null, "John Doe", "john@example.com");

        User savedUser = userRepository.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        User user = new User(null, "John Doe", "john@example.com");
        User savedUser = entityManager.persistAndFlush(user);

        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void findById_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findById(999L);

        assertThat(foundUser).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        User user1 = new User(null, "John Doe", "john@example.com");
        User user2 = new User(null, "Jane Smith", "jane@example.com");
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    void deleteById_ShouldDeleteUser() {
        User user = new User(null, "John Doe", "john@example.com");
        User savedUser = entityManager.persistAndFlush(user);

        userRepository.deleteById(savedUser.getId());

        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }
}