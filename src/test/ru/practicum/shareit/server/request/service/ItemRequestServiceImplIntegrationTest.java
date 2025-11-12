package ru.practicum.shareit.server.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // Создаем тестовых пользователей
        testUser1 = new User();
        testUser1.setName("Test User 1");
        testUser1.setEmail("test1@email.com");

        testUser2 = new User();
        testUser2.setName("Test User 2");
        testUser2.setEmail("test2@email.com");

        testUser1 = userService.create(testUser1);
        testUser2 = userService.create(testUser2);
    }

    @Test
    void create_shouldCreateItemRequest() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill for home repairs");

        // When
        ItemRequestDto createdRequest = itemRequestService.create(requestDto, testUser1.getId());

        // Then
        assertNotNull(createdRequest);
        assertNotNull(createdRequest.getId());
        assertEquals("Need a drill for home repairs", createdRequest.getDescription());
        assertNotNull(createdRequest.getCreated());
        assertTrue(createdRequest.getCreated().isBefore(java.time.LocalDateTime.now().plusSeconds(1)));
        assertTrue(createdRequest.getCreated().isAfter(java.time.LocalDateTime.now().minusSeconds(1)));
        assertNotNull(createdRequest.getItems());
        assertTrue(createdRequest.getItems().isEmpty());
    }

    @Test
    void create_shouldThrowExceptionWhenUserNotFound() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");
        Long nonExistentUserId = 999L;

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.create(requestDto, nonExistentUserId)
        );
    }

    @Test
    void getByUserId_shouldReturnUserRequests() {
        // Given
        ItemRequestDto request1 = new ItemRequestDto();
        request1.setDescription("Need a drill");
        itemRequestService.create(request1, testUser1.getId());

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setDescription("Need a hammer");
        itemRequestService.create(request2, testUser1.getId());

        // When
        List<ItemRequestDto> requests = itemRequestService.getByUserId(testUser1.getId());

        // Then
        assertNotNull(requests);
        assertEquals(2, requests.size());
        assertEquals("Need a hammer", requests.get(0).getDescription()); // Более новая первая
        assertEquals("Need a drill", requests.get(1).getDescription());
    }

    @Test
    void getByUserId_shouldReturnEmptyListWhenNoRequests() {
        // When
        List<ItemRequestDto> requests = itemRequestService.getByUserId(testUser1.getId());

        // Then
        assertNotNull(requests);
        assertTrue(requests.isEmpty());
    }

    @Test
    void getByUserId_shouldThrowExceptionWhenUserNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.getByUserId(999L)
        );
    }

    @Test
    void getAll_shouldReturnOtherUsersRequests() {
        // Given
        ItemRequestDto request1 = new ItemRequestDto();
        request1.setDescription("User1 request 1");
        itemRequestService.create(request1, testUser1.getId());

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setDescription("User1 request 2");
        itemRequestService.create(request2, testUser1.getId());

        ItemRequestDto request3 = new ItemRequestDto();
        request3.setDescription("User2 request");
        itemRequestService.create(request3, testUser2.getId());

        // When - User2 запрашивает все запросы кроме своих
        List<ItemRequestDto> requests = itemRequestService.getAll(testUser2.getId(), 0, 10);

        // Then
        assertNotNull(requests);
        assertEquals(2, requests.size()); // Только запросы User1
        assertEquals("User1 request 2", requests.get(0).getDescription()); // Более новые первыми
        assertEquals("User1 request 1", requests.get(1).getDescription());
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoOtherUsersRequests() {
        // When
        List<ItemRequestDto> requests = itemRequestService.getAll(testUser1.getId(), 0, 10);

        // Then
        assertNotNull(requests);
        assertTrue(requests.isEmpty());
    }

    @Test
    void getAll_shouldRespectPagination() {
        // Given
        {
            ItemRequestDto request = new ItemRequestDto();
            request.setDescription("Request " + 1);
            itemRequestService.create(request, testUser1.getId());
        }
        {
            ItemRequestDto request = new ItemRequestDto();
            request.setDescription("Request " + 2);
            itemRequestService.create(request, testUser1.getId());
        }
        {
            ItemRequestDto request = new ItemRequestDto();
            request.setDescription("Request " + 3);
            itemRequestService.create(request, testUser1.getId());
        }
        {
            ItemRequestDto request = new ItemRequestDto();
            request.setDescription("Request " + 4);
            itemRequestService.create(request, testUser1.getId());
        }
        {
            ItemRequestDto request = new ItemRequestDto();
            request.setDescription("Request " + 5);
            itemRequestService.create(request, testUser1.getId());
        }

        // When
        List<ItemRequestDto> firstPage = itemRequestService.getAll(testUser2.getId(), 0, 2);
        List<ItemRequestDto> secondPage = itemRequestService.getAll(testUser2.getId(), 2, 2);
        List<ItemRequestDto> thirdPage = itemRequestService.getAll(testUser2.getId(), 4, 2);

        // Then
        assertNotNull(firstPage);
        assertEquals(2, firstPage.size());
        assertEquals("Request 5", firstPage.get(0).getDescription());
        assertEquals("Request 4", firstPage.get(1).getDescription());

        assertNotNull(secondPage);
        assertEquals(2, secondPage.size());
        assertEquals("Request 3", secondPage.get(0).getDescription());
        assertEquals("Request 2", secondPage.get(1).getDescription());

        assertNotNull(thirdPage);
        assertEquals(1, thirdPage.size());
        assertEquals("Request 1", thirdPage.getFirst().getDescription());
    }

    @Test
    void getById_shouldReturnRequest() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need specific item");
        ItemRequestDto createdRequest = itemRequestService.create(requestDto, testUser1.getId());

        // When
        ItemRequestDto foundRequest = itemRequestService.getById(createdRequest.getId(), testUser2.getId());

        // Then
        assertNotNull(foundRequest);
        assertEquals(createdRequest.getId(), foundRequest.getId());
        assertEquals("Need specific item", foundRequest.getDescription());
        assertEquals(createdRequest.getCreated(), foundRequest.getCreated());
        assertNotNull(foundRequest.getItems());
    }

    @Test
    void getById_shouldThrowExceptionWhenRequestNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.getById(999L, testUser1.getId())
        );
    }

    @Test
    void getById_shouldThrowExceptionWhenUserNotFound() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need item");
        ItemRequestDto createdRequest = itemRequestService.create(requestDto, testUser1.getId());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.getById(createdRequest.getId(), 999L)
        );
    }

    @Test
    void integrationFlow_shouldWorkCorrectly() {
        // Comprehensive integration test
        // Step 1: Create requests
        ItemRequestDto request1 = new ItemRequestDto();
        request1.setDescription("First request");
        ItemRequestDto createdRequest1 = itemRequestService.create(request1, testUser1.getId());

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setDescription("Second request");
        itemRequestService.create(request2, testUser1.getId()); // Убрали неиспользуемую переменную

        // Step 2: Verify user can see their own requests
        List<ItemRequestDto> user1Requests = itemRequestService.getByUserId(testUser1.getId());
        assertEquals(2, user1Requests.size());
        assertEquals("Second request", user1Requests.get(0).getDescription());
        assertEquals("First request", user1Requests.get(1).getDescription());

        // Step 3: Verify other user can see all requests
        List<ItemRequestDto> allRequests = itemRequestService.getAll(testUser2.getId(), 0, 10);
        assertEquals(2, allRequests.size());

        // Step 4: Verify individual request retrieval
        ItemRequestDto retrievedRequest = itemRequestService.getById(createdRequest1.getId(), testUser2.getId());
        assertEquals("First request", retrievedRequest.getDescription());
    }

    @Test
    void getAll_shouldHandleZeroSizePage() {
        // Given
        ItemRequestDto request = new ItemRequestDto();
        request.setDescription("Test request");
        itemRequestService.create(request, testUser1.getId());

        // When
        List<ItemRequestDto> requests = itemRequestService.getAll(testUser2.getId(), 10, 5);

        // Then
        assertNotNull(requests);
        assertTrue(requests.isEmpty());
    }

    @Test
    void create_shouldHandleEmptyDescription() {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("   "); // Пробелы

        // When
        ItemRequestDto createdRequest = itemRequestService.create(requestDto, testUser1.getId());

        // Then
        assertNotNull(createdRequest);
        assertEquals("   ", createdRequest.getDescription());
    }
}