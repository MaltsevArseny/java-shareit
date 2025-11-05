package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

public class ItemServiceImplTest {

    private ItemRepository itemRepository;
    private ItemServiceImpl itemService;

    @BeforeEach
    public void setUp() {
        itemRepository = mock(ItemRepository.class);
        itemService = new ItemServiceImpl();
        // Используем рефлексию для установки мок-репозитория
        try {
            var field = ItemServiceImpl.class.getDeclaredField("itemRepository");
            field.setAccessible(true);
            field.set(itemService, itemRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateItem() {
        ItemDto itemDto = new ItemDto(null, "Дрель", "Мощная дрель", true);
        Item item = new Item(1L, "Дрель", "Мощная дрель", true, 1L);

        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(itemDto, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Дрель", result.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void testUpdateItemByOwner() {
        Item existingItem = new Item(1L, "Дрель", "Мощная дрель", true, 1L);
        ItemDto updateDto = new ItemDto(null, "Дрель Updated", "Новое описание", null);
        Item updatedItem = new Item(1L, "Дрель Updated", "Новое описание", true, 1L);

        when(itemRepository.findById(1L)).thenReturn(existingItem);
        when(itemRepository.update(any(Item.class))).thenReturn(updatedItem);

        ItemDto result = itemService.updateItem(1L, updateDto, 1L);

        assertEquals("Дрель Updated", result.getName());
        assertEquals("Новое описание", result.getDescription());
    }

    @Test
    public void testUpdateItemByNonOwner() {
        Item existingItem = new Item(1L, "Дрель", "Мощная дрель", true, 1L);

        when(itemRepository.findById(1L)).thenReturn(existingItem);

        ItemDto result = itemService.updateItem(1L, new ItemDto(), 2L);

        assertNull(result); // Не владелец не может редактировать
    }

    @Test
    public void testGetItemById() {
        Item item = new Item(1L, "Дрель", "Мощная дрель", true, 1L);

        when(itemRepository.findById(1L)).thenReturn(item);

        ItemDto result = itemService.getItemById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Дрель", result.getName());
    }

    @Test
    public void testGetAllItemsByOwnerId() {
        Item item1 = new Item(1L, "Дрель", "Мощная дрель", true, 1L);
        Item item2 = new Item(2L, "Молоток", "Простой молоток", true, 1L);

        when(itemRepository.findAllByOwnerId(1L)).thenReturn(List.of(item1, item2));

        List<ItemDto> result = itemService.getAllItemsByOwnerId(1L);

        assertEquals(2, result.size());
        assertEquals("Дрель", result.get(0).getName());
        assertEquals("Молоток", result.get(1).getName());
    }

    @Test
    public void testSearchItems() {
        Item item1 = new Item(1L, "Дрель", "Мощная дрель", true, 1L);
        Item item2 = new Item(2L, "Аккумуляторная дрель", "Беспроводная", true, 2L);

        when(itemRepository.search("дрель")).thenReturn(List.of(item1, item2));

        List<ItemDto> result = itemService.searchItems("дрель");

        assertEquals(2, result.size());
    }
}