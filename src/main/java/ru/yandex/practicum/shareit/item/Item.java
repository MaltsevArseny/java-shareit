package ru.yandex.practicum.shareit.item;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Item {
    // Getters and Setters
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long ownerId;

    public Item() {
    }

    public Item(Long id, String name, String description, Boolean available, Long ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.ownerId = ownerId;
    }

}