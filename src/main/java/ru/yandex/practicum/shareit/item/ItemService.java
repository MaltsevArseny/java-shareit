package ru.yandex.practicum.shareit.item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);
    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);
    ItemDto getItemById(Long itemId);
    List<ItemDto> getAllItemsByOwnerId(Long ownerId);
    List<ItemDto> searchItems(String text);
}