package gr.ots.pharmacy.warehouse.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferTest {

    private final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private Transfer transfer;

    @BeforeEach
    void setUp() {
        transfer = new Transfer();
        transfer.setType(TransferType.IN);
        transfer.setDrug(new Drug());
        transfer.setQuantity(100);
        transfer.setTransferDate(Instant.now());
        transfer.setCreatedAt(Instant.now());
    }

    @Test
    void validateType() {
        var violations = VALIDATOR.validate(transfer);
        assertTrue(violations.isEmpty());

        // type is null
        transfer.setType(null);
        violations = VALIDATOR.validate(transfer);
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
        assertEquals("type", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void validateDrug() {
        var violations = VALIDATOR.validate(transfer);
        assertTrue(violations.isEmpty());

        // type is null
        transfer.setDrug(null);
        violations = VALIDATOR.validate(transfer);
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
        assertEquals("drug", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void validateQuantity() {
        var violations = VALIDATOR.validate(transfer);
        assertTrue(violations.isEmpty());

        // quantity is 0
        transfer.setQuantity(0);
        violations = VALIDATOR.validate(transfer);
        assertTrue(violations.isEmpty());

        // quantity is negative
        transfer.setQuantity(-1);
        violations = VALIDATOR.validate(transfer);
        assertEquals(1, violations.size());
        assertEquals("must be greater than or equal to 0", violations.iterator().next().getMessage());
        assertEquals("quantity", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void validateTransferDate() {
        var violations = VALIDATOR.validate(transfer);
        assertTrue(violations.isEmpty());

        // transferDate is null
        transfer.setTransferDate(null);
        violations = VALIDATOR.validate(transfer);
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
        assertEquals("transferDate", violations.iterator().next().getPropertyPath().toString());
    }

}