package ru.yandex.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class ErrorHandlerTest {

    @Autowired
    private ErrorHandler errorHandler;

    @Test
    void handleMissingRequestHeader_ShouldReturnBadRequest() {
        // Arrange
        MissingRequestHeaderException exception = mock(MissingRequestHeaderException.class);
        when(exception.getMessage()).thenReturn("Required header 'X-Sharer-User-Id' is not present");

        // Act
        Map<String, String> response = errorHandler.handleMissingRequestHeader(exception);

        // Assert
        assertNotNull(response);
        assertEquals("Missing required header", response.get("error"));
        assertEquals("Required header 'X-Sharer-User-Id' is not present", response.get("message"));
    }

    @Test
    void handleMissingRequestHeader_WithNullMessage_ShouldHandleGracefully() {
        // Arrange
        MissingRequestHeaderException exception = mock(MissingRequestHeaderException.class);
        when(exception.getMessage()).thenReturn(null);

        // Act
        Map<String, String> response = errorHandler.handleMissingRequestHeader(exception);

        // Assert
        assertNotNull(response);
        assertEquals("Missing required header", response.get("error"));
        assertEquals("Required header is missing", response.get("message"));
    }

    @Test
    void handleMethodArgumentTypeMismatch_ShouldReturnBadRequest() {
        // Arrange
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getMessage()).thenReturn("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'");

        // Act
        Map<String, String> response = errorHandler.handleMethodArgumentTypeMismatch(exception);

        // Assert
        assertNotNull(response);
        assertEquals("Invalid parameter type", response.get("error"));
        assertEquals("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'",
                response.get("message"));
    }

    @Test
    void handleMethodArgumentTypeMismatch_WithNullMessage_ShouldHandleGracefully() {
        // Arrange
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getMessage()).thenReturn(null);

        // Act
        Map<String, String> response = errorHandler.handleMethodArgumentTypeMismatch(exception);

        // Assert
        assertNotNull(response);
        assertEquals("Invalid parameter type", response.get("error"));
        assertEquals("Invalid parameter type", response.get("message"));
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid user ID");

        // Act
        Map<String, String> response = errorHandler.handleIllegalArgumentException(exception);

        // Assert
        assertNotNull(response);
        assertEquals("Invalid argument", response.get("error"));
        assertEquals("Invalid user ID", response.get("message"));
    }

    @Test
    void handleIllegalArgumentException_WithNullMessage_ShouldHandleGracefully() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException();

        // Act
        Map<String, String> response = errorHandler.handleIllegalArgumentException(exception);

        // Assert
        assertNotNull(response);
        assertEquals("Invalid argument", response.get("error"));
        assertEquals("Invalid argument", response.get("message"));
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error occurred");

        // Act
        Map<String, String> response = errorHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals("Internal server error", response.get("error"));
        assertEquals("Unexpected error occurred", response.get("message"));
    }

    @Test
    void handleGenericException_WithNullMessage_ShouldHandleGracefully() {
        // Arrange
        Exception exception = new RuntimeException();

        // Act
        Map<String, String> response = errorHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals("Internal server error", response.get("error"));
        assertEquals("Internal server error occurred", response.get("message"));
    }
}