package ru.practicum.shareit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    private Long requesterId;
    private LocalDateTime created;
}