package ru.yandex.practicum.shareit.item;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    public ItemServiceImpl() {
        this.itemRepository = new ItemRepository();
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        Item item = ItemMapper.toItem(itemDto, ownerId);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(itemId);
        if (existingItem == null || !existingItem.getOwnerId().equals(ownerId)) {
            return null;
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

        Item updatedItem = itemRepository.update(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwnerId(Long ownerId) {
        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}