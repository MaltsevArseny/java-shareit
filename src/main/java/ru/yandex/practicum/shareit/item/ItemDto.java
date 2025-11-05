package ru.yandex.practicum.shareit.item;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ItemDto {
    // Getters and Setters
    private Long id;
    private String name;
    private String description;
    private Boolean available;

    public ItemDto() {
    }

    public ItemDto(Long id, String name, String description, Boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
    }

}