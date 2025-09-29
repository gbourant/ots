package gr.ots.pharmacy.warehouse.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrugTest {

    private final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private Drug drug;

    @BeforeEach
    void setUp() {
        drug = new Drug();
        drug.setName("Aspirin");
        drug.setCode("ASP100");
        drug.setPrice(BigDecimal.TEN);
        drug.setStock(100);
        drug.setCategory(new Category());
        drug.setCreatedAt(Instant.now());
    }

    @Test
    void validateName() {
        var violations = VALIDATOR.validate(drug);
        assertTrue(violations.isEmpty());

        // name is null
        drug.setName(null);
        violations = VALIDATOR.validate(drug);
        assertEquals(1, violations.size());
        assertEquals("must not be blank", violations.iterator().next().getMessage());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());

        // name is short < 2
        drug.setName("A");
        violations = VALIDATOR.validate(drug);
        assertEquals(1, violations.size());
        assertEquals("size must be between 2 and 100", violations.iterator().next().getMessage());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());

        // name is long > 100
        drug.setName("A".repeat(101));
        violations = VALIDATOR.validate(drug);
        assertEquals(1, violations.size());
        assertEquals("size must be between 2 and 100", violations.iterator().next().getMessage());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());

        // name is blank
        drug.setName("  ");
        violations = VALIDATOR.validate(drug);
        assertEquals(1, violations.size());
        assertEquals("must not be blank", violations.iterator().next().getMessage());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void validateCode() {
        var violations = VALIDATOR.validate(drug);
        assertTrue(violations.isEmpty());

        // code is null
        drug.setCode(null);
        violations = VALIDATOR.validate(drug);
        assertEquals(1, violations.size());
        assertEquals("must not be blank", violations.iterator().next().getMessage());
        assertEquals("code", violations.iterator().next().getPropertyPath().toString());

        // code is short < 2
        drug.setCode("A");
        violations = VALIDATOR.validate(drug);
        assertEquals(1, violations.size());
        assertEquals("size must be between 2 and 50", violations.iterator().next().getMessage());
        assertEquals("code", violations.iterator().next().getPropertyPath().toString());

        // code is long > 50
        drug.setCode("A".repeat(51));
        violations = VALIDATOR.validate(drug);
        assertEquals(1, violations.size());
        assertEquals("size must be between 2 and 50", violations.iterator().next().getMessage());
        assertEquals("code", violations.iterator().next().getPropertyPath().toString());

        // code is blank
        drug.setCode("  ");
        violations = VALIDATOR.validate(drug);
        assertEquals(1, violations.size());
        assertEquals("must not be blank", violations.iterator().next().getMessage());
        assertEquals("code", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void validatePrice() {
        var violations = VALIDATOR.validate(drug);
        assertTrue(violations.isEmpty());

        // price is 0
        drug.setPrice(BigDecimal.ZERO);
        violations = VALIDATOR.validate(drug);
        assertTrue(violations.isEmpty());

        // price is negative
        drug.setPrice(BigDecimal.valueOf(-1));
        violations = VALIDATOR.validate(drug);
        assertEquals(1, violations.size());
        assertEquals("must be greater than or equal to 0", violations.iterator().next().getMessage());
        assertEquals("price", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void validateStock() {
        var violations = VALIDATOR.validate(drug);
        assertTrue(violations.isEmpty());

        // stock is 0
        drug.setStock(0);
        violations = VALIDATOR.validate(drug);
        assertTrue(violations.isEmpty());

        // stock is negative
        drug.setStock(-1);
        violations = VALIDATOR.validate(drug);
        assertEquals(1, violations.size());
        assertEquals("must be greater than or equal to 0", violations.iterator().next().getMessage());
        assertEquals("stock", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void validateCategory() {
        var violations = VALIDATOR.validate(drug);
        assertTrue(violations.isEmpty());

        // category is null
        drug.setCategory(null);
        violations = VALIDATOR.validate(drug);
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
        assertEquals("category", violations.iterator().next().getPropertyPath().toString());
    }

}