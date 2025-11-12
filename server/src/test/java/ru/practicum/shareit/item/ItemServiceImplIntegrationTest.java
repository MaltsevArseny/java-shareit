package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getUserItems_ShouldReturnUserItems() {
        User owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        itemRepository.save(new Item(null, "Item 1", "Desc 1", true, owner, null));
        itemRepository.save(new Item(null, "Item 2", "Desc 2", true, owner, null));

        List<Item> result = itemService.getUserItems(owner.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item 1")));
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item 2")));
    }

    @Test
    void searchItems_ShouldReturnAvailableMatchingItems() {
        User owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        itemRepository.save(new Item(null, "Drill", "Powerful drill", true, owner, null));
        itemRepository.save(new Item(null, "Hammer", "Heavy hammer", true, owner, null));
        itemRepository.save(new Item(null, "Broken Drill", "Not working", false, owner, null));

        List<Item> result = itemService.searchItems("drill");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }
}