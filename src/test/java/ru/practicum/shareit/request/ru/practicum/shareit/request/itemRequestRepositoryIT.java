package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRequestRepositoryIT { // Исправлено название класса (с большой буквы)

    @Autowired
    private itemrequestrepository requestRepository; // Исправлено название

    @Autowired
    private UserRepository userRepository; // Добавлен UserRepository для сохранения пользователей

    ItemRequestRepositoryIT(TestEntityManager em) {
    }

    @Test
    void findAllByRequesterIdOrderByCreatedDesc_whenRequestsExist_thenReturnSortedList() {
        // given
        User user = createUser("user@email.com");
        User savedUser = userRepository.save(user); // Сохраняем через репозиторий

        ItemRequest request1 = createRequest(savedUser, "Need drill", LocalDateTime.now().minusDays(1));
        ItemRequest request2 = createRequest(savedUser, "Need hammer", LocalDateTime.now());
        requestRepository.save(request1);
        requestRepository.save(request2);

        // when
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdOrderByCreatedDesc(savedUser.getId());

        // then
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getDescription()).isEqualTo("Need hammer");
        assertThat(requests.get(1).getDescription()).isEqualTo("Need drill");
    }

    @Test
    void findAllByRequesterIdNotOrderByCreatedDesc_whenOtherUserRequestsExist_thenReturnList() {
        // given
        User user1 = createUser("user1@email.com");
        User user2 = createUser("user2@email.com");
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        ItemRequest request1 = createRequest(savedUser1, "Need drill", LocalDateTime.now());
        ItemRequest request2 = createRequest(savedUser2, "Need hammer", LocalDateTime.now().minusDays(1));
        requestRepository.save(request1);
        requestRepository.save(request2);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(savedUser1.getId(), pageable);

        // then
        assertThat(requests).hasSize(1);
        assertThat(requests.getFirst().getRequester().getId()).isEqualTo(savedUser2.getId());
        assertThat(requests.getFirst().getDescription()).isEqualTo("Need hammer");
    }

    @Test
    void findAllByRequesterIdOrderByCreatedDesc_whenNoRequests_thenReturnEmptyList() {
        // given
        User user = createUser("user@email.com");
        User savedUser = userRepository.save(user);

        // when
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdOrderByCreatedDesc(savedUser.getId());

        // then
        assertThat(requests).isEmpty();
    }

    @Test
    void findAllByRequesterIdNotOrderByCreatedDesc_whenNoOtherUserRequests_thenReturnEmptyList() {
        // given
        User user1 = createUser("user1@email.com");
        User savedUser1 = userRepository.save(user1);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(savedUser1.getId(), pageable);

        // then
        assertThat(requests).isEmpty();
    }

    @Test
    void findAllByRequesterIdNotOrderByCreatedDesc_withPagination_thenReturnCorrectPage() {
        // given
        User user1 = createUser("user1@email.com");
        User user2 = createUser("user2@email.com");
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        // Создаем несколько запросов для второго пользователя
        ItemRequest request1 = createRequest(savedUser2, "Request 1", LocalDateTime.now().minusDays(3));
        ItemRequest request2 = createRequest(savedUser2, "Request 2", LocalDateTime.now().minusDays(2));
        ItemRequest request3 = createRequest(savedUser2, "Request 3", LocalDateTime.now().minusDays(1));
        requestRepository.save(request1);
        requestRepository.save(request2);
        requestRepository.save(request3);

        Pageable pageable = PageRequest.of(0, 2); // Первая страница, 2 элемента

        // when
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(savedUser1.getId(), pageable);

        // then
        assertThat(requests).hasSize(2);
        // Должны вернуться самые свежие запросы
        assertThat(requests.get(0).getDescription()).isEqualTo("Request 3");
        assertThat(requests.get(1).getDescription()).isEqualTo("Request 2");
    }

    private User createUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        return user;
    }

    private ItemRequest createRequest(User user, String description, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequester(user);
        request.setCreated(created);
        return request;
    }
}