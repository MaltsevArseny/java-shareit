package ru.practicum.shareit.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.dto.ItemRequestDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validateItemRequestDto_whenDescriptionBlank_thenViolation() {
        // Используем конструктор с параметрами
        ItemRequestDto dto = new ItemRequestDto(1L, "");

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("cannot be blank"));

        // Используем геттеры
        assertEquals(1L, dto.getId());
        assertEquals("", dto.getDescription());
    }

    @Test
    void validateItemRequestDto_whenDescriptionTooLong_thenViolation() {
        String longDescription = "a".repeat(1001);
        // Используем сеттеры
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(2L);
        dto.setDescription(longDescription);

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("too long"));

        // Используем геттеры
        assertEquals(2L, dto.getId());
        assertEquals(longDescription, dto.getDescription());
    }

    @Test
    void validateItemRequestDto_whenValid_thenNoViolations() {
        // Используем конструктор по умолчанию и сеттеры
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(3L);
        dto.setDescription("Valid description");

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());

        // Используем геттеры
        assertEquals(3L, dto.getId());
        assertEquals("Valid description", dto.getDescription());
    }

    @Test
    void validateItemRequestDto_whenNullId_thenNoViolations() {
        // Используем конструктор с null id
        ItemRequestDto dto = new ItemRequestDto(null, "Valid description");

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertNull(dto.getId());
        assertEquals("Valid description", dto.getDescription());
    }
}