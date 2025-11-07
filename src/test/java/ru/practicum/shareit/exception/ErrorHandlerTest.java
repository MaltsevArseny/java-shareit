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
        assertThat(response.error()).isEqualTo("Entity not found");
    }

    @Test
    void handleValidationException_ShouldReturnErrorResponse() {
        ValidationException exception = new ValidationException("Validation failed");

        ErrorHandler.ErrorResponse response = errorHandler.handleValidationException(exception);

        assertThat(response).isNotNull();
        assertThat(response.error()).isEqualTo("Validation failed");
    }

    @Test
    void handleAccessDeniedException_ShouldReturnErrorResponse() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        ErrorHandler.ErrorResponse response = errorHandler.handleAccessDeniedException(exception);

        assertThat(response).isNotNull();
        assertThat(response.error()).isEqualTo("Access denied");
    }

    @Test
    void handleThrowable_ShouldReturnGenericError() {
        RuntimeException exception = new RuntimeException("Some error");

        ErrorHandler.ErrorResponse response = errorHandler.handleThrowable(exception);

        assertThat(response).isNotNull();
        assertThat(response.error()).isEqualTo("Произошла непредвиденная ошибка.");
    }

    @Test
    void errorResponse_ShouldHaveCorrectStructure() {
        ErrorHandler.ErrorResponse errorResponse = new ErrorHandler.ErrorResponse("Test error");

        assertThat(errorResponse.error()).isEqualTo("Test error");
    }

    @Test
    void validationErrorResponse_ShouldHaveCorrectStructure() {
        ErrorHandler.FieldError fieldError = new ErrorHandler.FieldError("field", "message");
        ErrorHandler.ValidationErrorResponse validationResponse =
                new ErrorHandler.ValidationErrorResponse("Validation error", java.util.List.of(fieldError));

        assertThat(validationResponse.error()).isEqualTo("Validation error");
        assertThat(validationResponse.fieldErrors()).hasSize(1);
        assertThat(validationResponse.fieldErrors().getFirst().field()).isEqualTo("field");
        assertThat(validationResponse.fieldErrors().getFirst().message()).isEqualTo("message");
    }

    @Test
    void fieldError_ShouldHaveCorrectStructure() {
        ErrorHandler.FieldError fieldError = new ErrorHandler.FieldError("email", "Invalid email format");

        assertThat(fieldError.field()).isEqualTo("email");
        assertThat(fieldError.message()).isEqualTo("Invalid email format");
    }
}