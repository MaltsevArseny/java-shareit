package ru.yandex.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMissingRequestHeader(MissingRequestHeaderException e) {
        String message = getMessageOrDefault(e, "Required header is missing");
        return Map.of("error", "Missing required header", "message", message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = getMessageOrDefault(e, "Invalid parameter type");
        return Map.of("error", "Invalid parameter type", "message", message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = getMessageOrDefault(e, "Invalid argument");
        return Map.of("error", "Invalid argument", "message", message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGenericException(Exception e) {
        String message = getMessageOrDefault(e, "Internal server error occurred");
        return Map.of("error", "Internal server error", "message", message);
    }

    private String getMessageOrDefault(Exception e, String defaultMessage) {
        if (e == null) {
            return defaultMessage;
        }
        String message = e.getMessage();
        return (message != null && !message.trim().isEmpty()) ? message : defaultMessage;
    }
}