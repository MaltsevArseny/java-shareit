package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ItemControllerIntegrationTest {

    @Autowired
    private ItemController itemController;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_ShouldUseService() {
        // Arrange
        ItemDto inputDto = new ItemDto(null, "Дрель", "Мощная дрель", true);
        ItemDto expectedDto = new ItemDto(1L, "Дрель", "Мощная дрель", true);

        when(itemService.createItem(any(ItemDto.class), eq(1L))).thenReturn(expectedDto);

        // Act
        ItemDto result = itemController.createItem(inputDto, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Дрель", result.getName());
        verify(itemService, times(1)).createItem(inputDto, 1L);
    }

    @Test
    void updateItem_ShouldUseService() {
        // Arrange
        ItemDto updateDto = new ItemDto(null, "Дрель Updated", "Новое описание", null);
        ItemDto expectedDto = new ItemDto(1L, "Дрель Updated", "Новое описание", true);

        when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(1L))).thenReturn(expectedDto);

        // Act
        ItemDto result = itemController.updateItem(1L, updateDto, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Дрель Updated", result.getName());
        verify(itemService, times(1)).updateItem(1L, updateDto, 1L);
    }

    @Test
    void getItemById_ShouldUseService() {
        // Arrange
        ItemDto expectedDto = new ItemDto(1L, "Дрель", "Мощная дрель", true);
        when(itemService.getItemById(1L)).thenReturn(expectedDto);

        // Act
        ItemDto result = itemController.getItemById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(itemService, times(1)).getItemById(1L);
    }

    @Test
    void getAllItemsByOwnerId_ShouldUseService() {
        // Arrange
        List<ItemDto> expectedItems = List.of(
                new ItemDto(1L, "Дрель", "Мощная дрель", true),
                new ItemDto(2L, "Молоток", "Простой молоток", true)
        );

        when(itemService.getAllItemsByOwnerId(1L)).thenReturn(expectedItems);

        // Act
        List<ItemDto> result = itemController.getAllItemsByOwnerId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(itemService, times(1)).getAllItemsByOwnerId(1L);
    }

    @Test
    void searchItems_ShouldUseService() {
        // Arrange
        List<ItemDto> expectedItems = List.of(
                new ItemDto(1L, "Дрель", "Мощная дрель", true)
        );

        when(itemService.searchItems("дрель")).thenReturn(expectedItems);

        // Act
        List<ItemDto> result = itemController.searchItems("дрель");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemService, times(1)).searchItems("дрель");
    }
}