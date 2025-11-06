package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ErrorHandlerTest {

    @Autowired
    private ErrorHandler errorHandler;

    @Test
    void handleEntityNotFoundException_ShouldReturnErrorResponse() {
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");

        ErrorHandler.ErrorResponse response = errorHandler.handleEntityNotFoundException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("Entity not found");
    }

    @Test
    void handleValidationException_ShouldReturnErrorResponse() {
        ValidationException exception = new ValidationException("Validation failed");

        ErrorHandler.ErrorResponse response = errorHandler.handleValidationException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("Validation failed");
    }

    @Test
    void handleAccessDeniedException_ShouldReturnErrorResponse() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        ErrorHandler.ErrorResponse response = errorHandler.handleAccessDeniedException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("Access denied");
    }

    @Test
    void handleThrowable_ShouldReturnGenericError() {
        RuntimeException exception = new RuntimeException("Some error");

        ErrorHandler.ErrorResponse response = errorHandler.handleThrowable(exception);

        assertThat(response).isNotNull();
        assertThat(response.getError()).isEqualTo("Произошла непредвиденная ошибка.");
    }

    @Test
    void errorResponse_ShouldHaveCorrectStructure() {
        ErrorHandler.ErrorResponse errorResponse = new ErrorHandler.ErrorResponse("Test error");

        assertThat(errorResponse.getError()).isEqualTo("Test error");
    }
}