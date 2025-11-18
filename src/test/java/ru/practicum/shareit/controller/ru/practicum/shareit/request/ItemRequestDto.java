package ru.practicum.shareit.request;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.ItemDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 1000, message = "Description too long")
    private String description;

    private Long requesterId;
    private LocalDateTime created;
    private List<ItemDto> items;

    public ItemRequestDto() {
    }

    public ItemRequestDto(Long id, String description) {
        this.id = id;
        this.description = description;
    }

    public ItemRequestDto(Long id, String description, Long requesterId, LocalDateTime created) {
        this.id = id;
        this.description = description;
        this.requesterId = requesterId;
        this.created = created;
    }
}