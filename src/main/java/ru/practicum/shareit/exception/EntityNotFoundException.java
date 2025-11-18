package ru.practicum.shareit.exception;

@SuppressWarnings("unused")
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}