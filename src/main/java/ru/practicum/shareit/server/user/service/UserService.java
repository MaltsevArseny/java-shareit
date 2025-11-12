package ru.practicum.shareit.server.user.service;

import ru.practicum.shareit.server.user.model.User;
import java.util.List;

public interface UserService {
    User create(User user);
    User update(Long userId, User user);
    User getUserById(Long userId);
    List<User> getAll();
    void delete(Long userId);
}