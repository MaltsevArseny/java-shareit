package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    public void testItemCreation() {
        Item item = new Item(1L, "Дрель", "Мощная дрель", true, 1L);

        assertEquals(1L, item.getId());
        assertEquals("Дрель", item.getName());
        assertEquals("Мощная дрель", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(1L, item.getOwnerId());
    }

    @Test
    public void testItemSetters() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwnerId(1L);

        assertEquals(1L, item.getId());
        assertEquals("Дрель", item.getName());
        assertEquals("Мощная дрель", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(1L, item.getOwnerId());
    }
}