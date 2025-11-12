// gateway/src/main/java/ru/practicum/shareit/dto/ItemRequestWithItemsDto.java
package ru.practicum.shareit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestWithItemsDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemDto> items;
}