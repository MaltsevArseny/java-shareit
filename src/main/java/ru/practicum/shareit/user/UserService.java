package ru.practicum.shareit.user;

public interface UserService {
    User getUserById(Long userId);
    boolean validateUserExists(Long userId);
}