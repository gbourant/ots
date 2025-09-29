package gr.ots.pharmacy.warehouse.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseEntityTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private BaseEntity baseEntity;

    @BeforeEach
    void setUp() {
        baseEntity = new BaseEntity();
        baseEntity.setCreatedAt(Instant.now());
        baseEntity.setUpdatedAt(Instant.now());
        baseEntity.setVersion(1);
    }

    @Test
    void validateCreatedAt() {
        var violations = VALIDATOR.validate(baseEntity);
        assertTrue(violations.isEmpty());

        // createdAt is null
        baseEntity.setCreatedAt(null);
        violations = VALIDATOR.validate(baseEntity);
        assertEquals(2, violations.size());
        var violationsList = violations.stream().sorted(Comparator.comparing(ConstraintViolation::getMessage)).toList();
        assertEquals("Updated date must be after created date", violationsList.get(0).getMessage());
        assertEquals("updateDateValid", violationsList.get(0).getPropertyPath().toString());
        assertEquals("must not be null", violationsList.get(1).getMessage());
        assertEquals("createdAt", violationsList.get(1).getPropertyPath().toString());
    }

    @Test
    void validateUpdatedAt() {
        var violations = VALIDATOR.validate(baseEntity);
        assertTrue(violations.isEmpty());

        // updatedAt is null
        baseEntity.setUpdatedAt(null);
        violations = VALIDATOR.validate(baseEntity);
        assertTrue(violations.isEmpty());

        // updatedAt is before createdAt
        baseEntity.setUpdatedAt(baseEntity.getCreatedAt().minusSeconds(1));
        violations = VALIDATOR.validate(baseEntity);
        assertEquals(1, violations.size());
        assertEquals("Updated date must be after created date", violations.iterator().next().getMessage());
        assertEquals("updateDateValid", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void validateVersion() {
        var violations = VALIDATOR.validate(baseEntity);
        assertTrue(violations.isEmpty());

        // version is 0
        baseEntity.setVersion(0);
        violations = VALIDATOR.validate(baseEntity);
        assertTrue(violations.isEmpty());

        // version is negative
        baseEntity.setVersion(-1);
        violations = VALIDATOR.validate(baseEntity);
        assertEquals(1, violations.size());
        assertEquals("must be greater than or equal to 0", violations.iterator().next().getMessage());
        assertEquals("version", violations.iterator().next().getPropertyPath().toString());
    }

}