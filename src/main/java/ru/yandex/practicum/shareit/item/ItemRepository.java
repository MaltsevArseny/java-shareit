package ru.yandex.practicum.shareit.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long idCounter = 1L;

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idCounter++);
        }
        items.put(item.getId(), item);
        return item;
    }

    public Item findById(Long id) {
        return items.get(id);
    }

    public List<Item> findAllByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> ownerId.equals(item.getOwnerId()))
                .collect(Collectors.toList());
    }

    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    public Item update(Item item) {
        if (items.containsKey(item.getId())) {
            items.put(item.getId(), item);
            return item;
        }
        return null;
    }

    public void deleteById(Long id) {
        items.remove(id);
    }

    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> item.getName().toLowerCase().contains(searchText) ||
                        item.getDescription().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
    }
}