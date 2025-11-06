package ru.practicum.shareit.exception;

@SuppressWarnings("unused")
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}