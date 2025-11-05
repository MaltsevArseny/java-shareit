package ru.yandex.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto testItemDto;
    private ItemDto createdItemDto;

    @BeforeEach
    void setUp() {
        testItemDto = new ItemDto(null, "Дрель", "Мощная дрель", true);
        createdItemDto = new ItemDto(1L, "Дрель", "Мощная дрель", true);
    }

    @Test
    void createItem_ShouldCallServiceAndReturnItem() throws Exception {
        // Arrange
        when(itemService.createItem(any(ItemDto.class), eq(1L))).thenReturn(createdItemDto);

        // Act & Assert
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Мощная дрель"))
                .andExpect(jsonPath("$.available").value(true));

        // Verify that service method was called
        verify(itemService, times(1)).createItem(any(ItemDto.class), eq(1L));
    }

    @Test
    void updateItem_ShouldCallServiceAndReturnUpdatedItem() throws Exception {
        // Arrange
        ItemDto updateDto = new ItemDto(null, "Дрель Updated", "Новое описание", null);
        ItemDto updatedItemDto = new ItemDto(1L, "Дрель Updated", "Новое описание", true);

        when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(1L))).thenReturn(updatedItemDto);

        // Act & Assert
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель Updated"))
                .andExpect(jsonPath("$.description").value("Новое описание"))
                .andExpect(jsonPath("$.available").value(true));

        // Verify that service method was called
        verify(itemService, times(1)).updateItem(eq(1L), any(ItemDto.class), eq(1L));
    }

    @Test
    void getItemById_ShouldCallServiceAndReturnItem() throws Exception {
        // Arrange
        when(itemService.getItemById(1L)).thenReturn(createdItemDto);

        // Act & Assert
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Мощная дрель"));

        // Verify that service method was called
        verify(itemService, times(1)).getItemById(1L);
    }

    @Test
    void getAllItemsByOwnerId_ShouldCallServiceAndReturnItemsList() throws Exception {
        // Arrange
        List<ItemDto> items = List.of(
                new ItemDto(1L, "Дрель", "Мощная дрель", true),
                new ItemDto(2L, "Молоток", "Простой молоток", true)
        );

        when(itemService.getAllItemsByOwnerId(1L)).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Молоток"));

        // Verify that service method was called
        verify(itemService, times(1)).getAllItemsByOwnerId(1L);
    }

    @Test
    void searchItems_ShouldCallServiceAndReturnMatchingItems() throws Exception {
        // Arrange
        List<ItemDto> foundItems = List.of(
                new ItemDto(1L, "Дрель", "Мощная дрель", true),
                new ItemDto(3L, "Аккумуляторная дрель", "Беспроводная", true)
        );

        when(itemService.searchItems("дрель")).thenReturn(foundItems);

        // Act & Assert
        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].name").value("Аккумуляторная дрель"));

        // Verify that service method was called
        verify(itemService, times(1)).searchItems("дрель");
    }

    @Test
    void searchItems_WithEmptyText_ShouldCallServiceAndReturnEmptyList() throws Exception {
        // Arrange
        when(itemService.searchItems("")).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify that service method was called
        verify(itemService, times(1)).searchItems("");
    }

    @Test
    void createItem_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required header"))
                .andExpect(jsonPath("$.message").exists());

        // Verify that service method was NOT called
        verify(itemService, never()).createItem(any(ItemDto.class), anyLong());
    }

    @Test
    void updateItem_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required header"))
                .andExpect(jsonPath("$.message").exists());

        // Verify that service method was NOT called
        verify(itemService, never()).updateItem(anyLong(), any(ItemDto.class), anyLong());
    }

    @Test
    void getAllItemsByOwnerId_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required header"))
                .andExpect(jsonPath("$.message").exists());

        // Verify that service method was NOT called
        verify(itemService, never()).getAllItemsByOwnerId(anyLong());
    }

    @Test
    void getItemById_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/items/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter type"))
                .andExpect(jsonPath("$.message").exists());

        // Verify that service method was NOT called
        verify(itemService, never()).getItemById(anyLong());
    }

    @Test
    void createItem_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(itemService.createItem(any(ItemDto.class), anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItemDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"))
                .andExpect(jsonPath("$.message").value("Database error"));

        // Verify that service method was called
        verify(itemService, times(1)).createItem(any(ItemDto.class), anyLong());
    }

    @Test
    void updateItem_ByNonOwner_ShouldCallServiceAndReturnNull() throws Exception {
        // Arrange
        when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(2L))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItemDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        // Verify that service method was called
        verify(itemService, times(1)).updateItem(eq(1L), any(ItemDto.class), eq(2L));
    }
}