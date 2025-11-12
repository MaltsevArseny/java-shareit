package ru.practicum.shareit.server.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.item.repository.ItemRepository;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.request.repository.ItemRequestRepository;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId) {
        User requester = userService.getUserById(userId);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        return convertToDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getByUserId(Long userId) {
        userService.getUserById(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId);
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId, Integer from, Integer size) {
        userService.getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdNot(userId, pageable);
        return requests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Long requestId, Long userId) {
        userService.getUserById(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Item request not found"));
        return convertToDto(itemRequest);
    }

    private ItemRequestDto convertToDto(ItemRequest itemRequest) {
        List<Item> items = itemRepository.findByRequestId(itemRequest.getId());
        List<ItemRequestDto.ItemDto> itemDtos = items != null ? items.stream()
                .map(item -> new ItemRequestDto.ItemDto(
                        item.getId(),
                        item.getName(),
                        item.getOwner().getId(),
                        item.getDescription(),
                        item.getAvailable(),
                        itemRequest.getId()
                ))
                .collect(Collectors.toList()) : Collections.emptyList();

        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                itemDtos
        );
    }
}