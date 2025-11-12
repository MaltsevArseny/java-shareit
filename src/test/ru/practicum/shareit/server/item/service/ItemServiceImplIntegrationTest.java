package ru.practicum.shareit.server.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.service.ItemRequestService;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemRequestService itemRequestService;

    private User owner;
    private ItemRequestDto itemRequest;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");
        owner = userService.create(owner);

        User requester = new User();
        requester.setName("Requester");
        requester.setEmail("requester@email.com");
        requester = userService.create(requester);

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a power drill");
        itemRequest = itemRequestService.create(requestDto, requester.getId());
    }

    @Test
    void create_shouldCreateItemWithRequest() {
        // Given
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Power Drill");
        itemDto.setDescription("High quality power drill");
        itemDto.setAvailable(true);
        itemDto.setRequestId(itemRequest.getId());

        // When
        ItemDto createdItem = itemService.create(itemDto, owner.getId());

        // Then
        assertNotNull(createdItem);
        assertEquals("Power Drill", createdItem.getName());
        assertEquals(itemRequest.getId(), createdItem.getRequestId());
    }

    @Test
    void getUserItems_shouldReturnUserItems() {
        // Given
        ItemDto item1 = new ItemDto();
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setAvailable(true);
        itemService.create(item1, owner.getId());

        ItemDto item2 = new ItemDto();
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        item2.setAvailable(true);
        itemService.create(item2, owner.getId());

        // When
        List<ItemDto> userItems = itemService.getUserItems(owner.getId());

        // Then
        assertNotNull(userItems);
        assertEquals(2, userItems.size());
    }

    @Test
    void search_shouldReturnAvailableItems() {
        // Given
        ItemDto item1 = new ItemDto();
        item1.setName("Power Drill");
        item1.setDescription("High quality power drill");
        item1.setAvailable(true);
        itemService.create(item1, owner.getId());

        ItemDto item2 = new ItemDto();
        item2.setName("Hammer");
        item2.setDescription("Steel hammer");
        item2.setAvailable(false); // Недоступен
        itemService.create(item2, owner.getId());

        // When
        List<ItemDto> searchResults = itemService.search("drill", 0, 10);

        // Then
        assertNotNull(searchResults);
        assertEquals(1, searchResults.size());
        assertEquals("Power Drill", searchResults.getFirst().getName());
    }
}