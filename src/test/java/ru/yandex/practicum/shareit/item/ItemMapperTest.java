package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemMapperTest {

    @Test
    public void testToItemDto() {
        Item item = new Item(1L, "Дрель", "Мощная дрель", true, 1L);
        ItemDto itemDto = ItemMapper.toItemDto(item);

        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
    }

    @Test
    public void testToItem() {
        ItemDto itemDto = new ItemDto(1L, "Дрель", "Мощная дрель", true);
        Long ownerId = 1L;
        Item item = ItemMapper.toItem(itemDto, ownerId);

        assertEquals(itemDto.getId(), item.getId());
        assertEquals(itemDto.getName(), item.getName());
        assertEquals(itemDto.getDescription(), item.getDescription());
        assertEquals(itemDto.getAvailable(), item.getAvailable());
        assertEquals(ownerId, item.getOwnerId());
    }

    @Test
    public void testToItemDtoWithNull() {
        assertNull(ItemMapper.toItemDto(null));
    }

    @Test
    public void testToItemWithNull() {
        assertNull(ItemMapper.toItem(null, 1L));
    }
}