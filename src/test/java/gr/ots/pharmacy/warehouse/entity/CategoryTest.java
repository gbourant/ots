package gr.ots.pharmacy.warehouse.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validateName() {
        Category category = new Category();
        category.setName("CAT");
        category.setCreatedAt(Instant.now());

        var violations = VALIDATOR.validate(category);
        assertTrue(violations.isEmpty());

        // name is null
        category.setName(null);
        violations = VALIDATOR.validate(category);
        assertEquals(1, violations.size());
        assertEquals("must not be blank", violations.iterator().next().getMessage());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());

        // name is short < 2
        category.setName("A");
        violations = VALIDATOR.validate(category);
        assertEquals(1, violations.size());
        assertEquals("size must be between 2 and 100", violations.iterator().next().getMessage());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());

        // name is long > 100
        category.setName("A".repeat(101));
        violations = VALIDATOR.validate(category);
        assertEquals(1, violations.size());
        assertEquals("size must be between 2 and 100", violations.iterator().next().getMessage());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());

        // name is blank
        category.setName("  ");
        violations = VALIDATOR.validate(category);
        assertEquals(1, violations.size());
        assertEquals("must not be blank", violations.iterator().next().getMessage());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());
    }
}