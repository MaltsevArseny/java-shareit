package ru.practicum.shareit.server.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.item.repository.ItemRepository;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.request.repository.ItemRequestRepository;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        User owner = userService.getUserById(userId);
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);

        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new IllegalArgumentException("Item request not found with id: " + itemDto.getRequestId()));
            item.setRequest(itemRequest);
        }

        Item savedItem = itemRepository.saveAndFlush(item);
        return convertToDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        // Проверяем существование пользователя
        userService.getUserById(userId);

        Item existingItem = itemRepository.findBy(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + itemId));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not the owner of this item");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return convertToDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        userService.getUserById(userId); // Проверяем существование пользователя
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + itemId));
        return convertToDto(item);
    }

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        userService.getUserById(userId);
        List<Item> items = itemRepository.findByOwnerId(userId);
        return items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text, Integer from, Integer size) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        // Валидация параметров пагинации
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters: from=" + from + ", size=" + size);
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.searchAvailableItems(text, pageable);

        return items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ItemDto convertToDto(Item item) {
        Long requestId = item.getRequest() != null ? item.getRequest().getId() : null;
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                requestId
        );
    }
}