package ru.practicum.shareit.server.item.service;

import ru.practicum.shareit.server.item.dto.ItemDto;
import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long userId);
    ItemDto update(Long itemId, ItemDto itemDto, Long userId);
    ItemDto getById(Long itemId, Long userId);
    List<ItemDto> getUserItems(Long userId);
    List<ItemDto> search(String text, Integer from, Integer size);
}