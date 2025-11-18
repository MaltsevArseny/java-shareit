package ru.practicum.shareit.server.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.service.ItemService;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestWithItemsIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private User requester;
    private User owner;
    private ItemRequestDto itemRequest;

    @BeforeEach
    void setUp() {
        // Создаем пользователя-инициатора запроса
        requester = new User();
        requester.setName("Requester");
        requester.setEmail("requester@email.com");
        requester = userService.create(requester);

        // Создаем пользователя-владельца вещи
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");
        owner = userService.create(owner);

        // Создаем запрос на вещь
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a laptop for work");
        itemRequest = itemRequestService.create(requestDto, requester.getId());
    }

    @Test
    void shouldReturnRequestWithItemsWhenItemsAdded() {
        // Given - создаем вещь в ответ на запрос
        ItemDto itemDto = new ItemDto();
        itemDto.setName("MacBook Pro");
        itemDto.setDescription("Apple MacBook Pro 16 inch");
        itemDto.setAvailable(true);
        itemDto.setRequestId(itemRequest.getId());
        itemService.create(itemDto, owner.getId());

        // When - получаем запрос
        ItemRequestDto retrievedRequest = itemRequestService.getById(itemRequest.getId(), requester.getId());

        // Then - проверяем, что вещь прикреплена к запросу
        assertNotNull(retrievedRequest.getItems());
        assertEquals(1, retrievedRequest.getItems().size());

        ItemRequestDto.ItemDto responseItem = retrievedRequest.getItems().getFirst();
        assertEquals("MacBook Pro", responseItem.getName());
        assertEquals(owner.getId(), responseItem.getOwnerId());
        assertEquals(itemRequest.getId(), responseItem.getRequestId());
        assertTrue(responseItem.getAvailable());
    }

    @Test
    void shouldReturnEmptyItemsListWhenNoItemsAdded() {
        // When
        ItemRequestDto retrievedRequest = itemRequestService.getById(itemRequest.getId(), requester.getId());

        // Then
        assertNotNull(retrievedRequest.getItems());
        assertTrue(retrievedRequest.getItems().isEmpty());
    }

    @Test
    void shouldShowItemsInAllRequestsList() {
        // Given
        ItemDto itemDto = new ItemDto();
        itemDto.setName("MacBook Pro");
        itemDto.setDescription("Apple MacBook Pro 16 inch");
        itemDto.setAvailable(true);
        itemDto.setRequestId(itemRequest.getId());
        itemService.create(itemDto, owner.getId());

        // When - владелец запрашивает все запросы (кроме своих)
        List<ItemRequestDto> allRequests = itemRequestService.getAll(owner.getId(), 0, 10);

        // Then
        assertNotNull(allRequests);
        assertEquals(1, allRequests.size());
        assertEquals(1, allRequests.getFirst().getItems().size());
        assertEquals("MacBook Pro", allRequests.getFirst().getItems().getFirst().getName());
    }

    @Test
    void shouldNotShowOwnRequestsInAllRequestsList() {
        // Given - создаем еще один запрос от владельца
        ItemRequestDto ownersRequest = new ItemRequestDto();
        ownersRequest.setDescription("Owners request");
        itemRequestService.create(ownersRequest, owner.getId());

        // When - владелец запрашивает все запросы
        List<ItemRequestDto> allRequests = itemRequestService.getAll(owner.getId(), 0, 10);

        // Then - видит только запросы других пользователей
        assertNotNull(allRequests);
        assertEquals(1, allRequests.size()); // Только запрос от requester
        assertEquals("Need a laptop for work", allRequests.getFirst().getDescription());
    }
}