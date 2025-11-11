package ru.practicum.shareit.server.request.service;

import ru.practicum.shareit.server.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId);
    List<ItemRequestDto> getByUserId(Long userId);
    List<ItemRequestDto> getAll(Long userId, Integer from, Integer size);
    ItemRequestDto getById(Long requestId, Long userId);
}