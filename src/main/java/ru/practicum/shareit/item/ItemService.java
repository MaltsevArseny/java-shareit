package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(ItemDto itemDto, Long itemId, Long userId);

    ItemWithBookingsDto getItemById(Long itemId, Long userId);

    List<ItemWithBookingsDto> getUserItems(Long userId, int from, int size);

    List<ItemDto> searchItems(String text, int from, int size);

    CommentResponseDto addComment(Long itemId, CommentRequestDto commentDto, Long userId);
}