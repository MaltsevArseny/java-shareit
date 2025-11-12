package ru.practicum.shareit.gateway.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class ItemRequestDtoValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void shouldValidateWhenDescriptionIsBlank() {
        // Given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("");

        // When
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Description cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void shouldValidateWhenDescriptionIsNull() {
        // Given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription(null);

        // When
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Description cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void shouldPassValidationWhenDescriptionIsValid() {
        // Given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Valid description");

        // When
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty());
    }
}