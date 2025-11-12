package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {
    Item createItem(Item item);

    Item updateItem(Long itemId, Item item);

    Item getItemById(Long itemId);

    List<Item> getUserItems(Long userId);

    List<Item> searchItems(String text);

    List<Item> getItemsByRequestId(Long requestId);
}