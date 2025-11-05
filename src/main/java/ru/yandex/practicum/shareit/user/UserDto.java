package ru.yandex.practicum.shareit.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDto {
    // Getters and Setters
    private Long id;
    private String name;
    private String email;

    public UserDto() {
    }

    public UserDto(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

}