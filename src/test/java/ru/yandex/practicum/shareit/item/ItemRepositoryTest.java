package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRepositoryTest {

    private ItemRepository itemRepository;
    private Item testItem1;
    private Item testItem2;
    private Item testItem3;

    @BeforeEach
    void setUp() {
        itemRepository = new ItemRepository();

        testItem1 = new Item(null, "Дрель", "Мощная дрель", true, 1L);
        testItem2 = new Item(null, "Молоток", "Простой молоток", true, 1L);
        testItem3 = new Item(null, "Пила", "Острая пила", false, 2L);
    }

    @Test
    void save_ShouldAssignIdAndSaveItem() {
        // Act
        Item savedItem = itemRepository.save(testItem1);

        // Assert
        assertNotNull(savedItem.getId());
        assertEquals("Дрель", savedItem.getName());
        assertEquals(1L, savedItem.getOwnerId());
    }

    @Test
    void save_WithExistingId_ShouldUpdateItem() {
        // Arrange
        Item savedItem = itemRepository.save(testItem1);
        Long itemId = savedItem.getId();

        // Act - update the item
        savedItem.setName("Дрель Updated");
        Item updatedItem = itemRepository.save(savedItem);

        // Assert
        assertEquals(itemId, updatedItem.getId());
        assertEquals("Дрель Updated", updatedItem.getName());
    }

    @Test
    void findById_ShouldReturnItem() {
        // Arrange
        Item savedItem = itemRepository.save(testItem1);
        Long itemId = savedItem.getId();

        // Act
        Item foundItem = itemRepository.findById(itemId);

        // Assert
        assertNotNull(foundItem);
        assertEquals(itemId, foundItem.getId());
        assertEquals("Дрель", foundItem.getName());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnNull() {
        // Act
        Item foundItem = itemRepository.findById(999L);

        // Assert
        assertNull(foundItem);
    }

    @Test
    void findAllByOwnerId_ShouldReturnOnlyOwnersItems() {
        // Arrange
        itemRepository.save(testItem1); // ownerId = 1
        itemRepository.save(testItem2); // ownerId = 1
        itemRepository.save(testItem3); // ownerId = 2

        // Act
        List<Item> user1Items = itemRepository.findAllByOwnerId(1L);
        List<Item> user2Items = itemRepository.findAllByOwnerId(2L);

        // Assert
        assertEquals(2, user1Items.size());
        assertEquals(1, user2Items.size());

        // Verify user1 items
        assertTrue(user1Items.stream().allMatch(item -> item.getOwnerId().equals(1L)));

        // Verify user2 items
        assertEquals("Пила", user2Items.get(0).getName());
    }

    @Test
    void findAll_ShouldReturnAllItems() {
        // Arrange
        itemRepository.save(testItem1);
        itemRepository.save(testItem2);
        itemRepository.save(testItem3);

        // Act
        List<Item> allItems = itemRepository.findAll();

        // Assert
        assertEquals(3, allItems.size());
        assertTrue(allItems.stream().anyMatch(item -> item.getName().equals("Дрель")));
        assertTrue(allItems.stream().anyMatch(item -> item.getName().equals("Молоток")));
        assertTrue(allItems.stream().anyMatch(item -> item.getName().equals("Пила")));
    }

    @Test
    void findAll_WithEmptyRepository_ShouldReturnEmptyList() {
        // Act
        List<Item> allItems = itemRepository.findAll();

        // Assert
        assertNotNull(allItems);
        assertTrue(allItems.isEmpty());
    }

    @Test
    void update_ShouldUpdateExistingItem() {
        // Arrange
        Item savedItem = itemRepository.save(testItem1);
        Long itemId = savedItem.getId();

        // Act - update the item
        savedItem.setName("Дрель Professional");
        savedItem.setDescription("Профессиональная дрель");
        savedItem.setAvailable(false);
        Item updatedItem = itemRepository.update(savedItem);

        // Assert
        assertNotNull(updatedItem);
        assertEquals(itemId, updatedItem.getId());
        assertEquals("Дрель Professional", updatedItem.getName());
        assertEquals("Профессиональная дрель", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void update_WithNonExistentItem_ShouldReturnNull() {
        // Arrange
        Item nonExistentItem = new Item(999L, "Non-existent", "Does not exist", true, 1L);

        // Act
        Item result = itemRepository.update(nonExistentItem);

        // Assert
        assertNull(result);
    }

    @Test
    void deleteById_ShouldRemoveItem() {
        // Arrange
        Item savedItem = itemRepository.save(testItem1);
        Long itemId = savedItem.getId();

        // Verify item exists before deletion
        assertNotNull(itemRepository.findById(itemId));

        // Act
        itemRepository.deleteById(itemId);

        // Assert
        assertNull(itemRepository.findById(itemId));
    }

    @Test
    void deleteById_WithNonExistentId_ShouldDoNothing() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> itemRepository.deleteById(999L));
    }

    @Test
    void search_ShouldReturnAvailableItemsMatchingText() {
        // Arrange
        itemRepository.save(testItem1); // Дрель - available
        itemRepository.save(testItem2); // Молоток - available
        itemRepository.save(testItem3); // Пила - not available

        // Act
        List<Item> foundItems = itemRepository.search("дрель");

        // Assert
        assertEquals(1, foundItems.size());
        assertEquals("Дрель", foundItems.get(0).getName());
        assertTrue(foundItems.get(0).getAvailable());
    }

    @Test
    void search_ShouldMatchNameAndDescription() {
        // Arrange
        Item item1 = new Item(null, "Аккумулятор", "Для дрели", true, 1L);
        Item item2 = new Item(null, "Дрель", "Аккумуляторная дрель", true, 1L);
        itemRepository.save(item1);
        itemRepository.save(item2);

        // Act
        List<Item> foundItems = itemRepository.search("аккумулятор");

        // Assert
        assertEquals(2, foundItems.size());
        assertTrue(foundItems.stream().anyMatch(item -> item.getName().equals("Аккумулятор")));
        assertTrue(foundItems.stream().anyMatch(item -> item.getDescription().contains("Аккумуляторная")));
    }

    @Test
    void search_ShouldNotReturnUnavailableItems() {
        // Arrange
        Item unavailableItem = new Item(null, "Дрель", "Сломанная дрель", false, 1L);
        itemRepository.save(unavailableItem);

        // Act
        List<Item> foundItems = itemRepository.search("дрель");

        // Assert
        assertTrue(foundItems.isEmpty());
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() {
        // Arrange
        itemRepository.save(testItem1);

        // Act
        List<Item> foundItems = itemRepository.search("");

        // Assert
        assertNotNull(foundItems);
        assertTrue(foundItems.isEmpty());
    }

    @Test
    void search_WithNullText_ShouldReturnEmptyList() {
        // Arrange
        itemRepository.save(testItem1);

        // Act
        List<Item> foundItems = itemRepository.search(null);

        // Assert
        assertNotNull(foundItems);
        assertTrue(foundItems.isEmpty());
    }

    @Test
    void search_ShouldBeCaseInsensitive() {
        // Arrange
        itemRepository.save(testItem1);

        // Act - search with different cases
        List<Item> foundItems1 = itemRepository.search("ДРЕЛЬ");
        List<Item> foundItems2 = itemRepository.search("дрель");
        List<Item> foundItems3 = itemRepository.search("ДрЕлЬ");

        // Assert
        assertEquals(1, foundItems1.size());
        assertEquals(1, foundItems2.size());
        assertEquals(1, foundItems3.size());
    }

    @Test
    void idCounter_ShouldIncrementForNewItems() {
        // Act
        Item item1 = itemRepository.save(testItem1);
        Item item2 = itemRepository.save(testItem2);
        Item item3 = itemRepository.save(testItem3);

        // Assert
        assertEquals(1L, item1.getId());
        assertEquals(2L, item2.getId());
        assertEquals(3L, item3.getId());
    }
}