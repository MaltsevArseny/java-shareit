package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_ShouldReturnSavedItem() {
        Item item = new Item(null, "Item", "Description", true, null, null);
        Item savedItem = new Item(1L, "Item", "Description", true, null, null);

        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        Item result = itemService.createItem(item);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Item", result.getName());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void updateItem_ShouldUpdateItemFields() {
        Long itemId = 1L;
        Item existingItem = new Item(itemId, "Old Name", "Old Desc", true, null, null);
        Item updateData = new Item(null, "New Name", "New Desc", false, null, null);
        Item updatedItem = new Item(itemId, "New Name", "New Desc", false, null, null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        Item result = itemService.updateItem(itemId, updateData);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertFalse(result.getAvailable());
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(existingItem);
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() {
        Long itemId = 1L;
        Item item = new Item(itemId, "Item", "Description", true, null, null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Item result = itemService.getItemById(itemId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    void getUserItems_ShouldReturnUserItems() {
        Long userId = 1L;
        List<Item> items = List.of(
                new Item(1L, "Item 1", "Desc 1", true, null, null),
                new Item(2L, "Item 2", "Desc 2", true, null, null)
        );

        when(itemRepository.findByOwnerIdOrderById(userId)).thenReturn(items);

        List<Item> result = itemService.getUserItems(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(itemRepository, times(1)).findByOwnerIdOrderById(userId);
    }

    @Test
    void searchItems_WithValidText_ShouldReturnMatchingItems() {
        String searchText = "item";
        List<Item> items = List.of(
                new Item(1L, "Item 1", "Description", true, null, null)
        );

        when(itemRepository.searchAvailableItems(searchText)).thenReturn(items);

        List<Item> result = itemService.searchItems(searchText);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemRepository, times(1)).searchAvailableItems(searchText);
    }

    @Test
    void searchItems_WithBlankText_ShouldReturnEmptyList() {
        List<Item> result = itemService.searchItems("   ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableItems(anyString());
    }

    // Добавим в ItemServiceImplTest
    @Test
    void getItemsByRequestId_ShouldReturnItemsForRequest() {
        Long requestId = 1L;
        List<Item> items = List.of(
                new Item(1L, "Item 1", "Desc 1", true, null, null),
                new Item(2L, "Item 2", "Desc 2", true, null, null)
        );

        when(itemRepository.findByRequestId(requestId)).thenReturn(items);

        List<Item> result = itemService.getItemsByRequestId(requestId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(itemRepository, times(1)).findByRequestId(requestId);
    }
}