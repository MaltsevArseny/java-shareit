package ru.practicum.shareit.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Setter
@Getter
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 1000, message = "Description too long")
    private String description;

    public ItemRequestDto() {
    }

    public ItemRequestDto(Long id, String description) {
        this.id = id;
        this.description = description;
    }
}