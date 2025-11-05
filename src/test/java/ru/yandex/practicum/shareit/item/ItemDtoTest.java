package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemDtoTest {

    @Test
    public void testItemDtoCreation() {
        ItemDto itemDto = new ItemDto(1L, "Дрель", "Мощная дрель", true);

        assertEquals(1L, itemDto.getId());
        assertEquals("Дрель", itemDto.getName());
        assertEquals("Мощная дрель", itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
    }

    @Test
    public void testItemDtoSetters() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);

        assertEquals(1L, itemDto.getId());
        assertEquals("Дрель", itemDto.getName());
        assertEquals("Мощная дрель", itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
    }
}