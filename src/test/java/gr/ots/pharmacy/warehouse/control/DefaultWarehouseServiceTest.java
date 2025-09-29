package gr.ots.pharmacy.warehouse.control;

import gr.ots.pharmacy.warehouse.boundary.DrugCreateDTO;
import gr.ots.pharmacy.warehouse.entity.Category;
import gr.ots.pharmacy.warehouse.entity.Drug;
import gr.ots.pharmacy.warehouse.entity.Transfer;
import gr.ots.pharmacy.warehouse.entity.TransferType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.flywaydb.core.Flyway;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class DefaultWarehouseServiceTest {

    @Inject
    WarehouseService warehouseService;

    @Inject
    Flyway flyway;

    @BeforeEach
    void clean() {
        flyway.clean();
        flyway.migrate();
    }

    private Drug createDrug() {
        Category category = new Category();
        category.setName("Test Category");
        category.persist();

        DrugCreateDTO dto = new DrugCreateDTO("Test Drug", "TEST001", new BigDecimal("10.99"), 100, category.id.intValue());
        return warehouseService.createDrug(dto);
    }

    @Test
    @Transactional
    void testCreateDrug() {
        Drug drug = createDrug();

        assertNotNull(drug);
        assertEquals("Test Drug", drug.getName());
        assertEquals("TEST001", drug.getCode());
        assertEquals(new BigDecimal("10.99"), drug.getPrice());
        assertEquals(100, drug.getStock());
        assertNotNull(drug.getCategory());
        assertEquals("Test Category", drug.getCategory().getName());
    }

    @Test
    @Transactional
    void testCreateDrugDuplicateCode() {
        createDrug();

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            createDrug();
            Drug.flush();
        });

        assertTrue(exception.getMessage().startsWith("could not execute statement [Unique index or primary key violation"));
    }

    @Test
    @Transactional
    void testGetAllDrugs() {
        assertTrue(warehouseService.getAllDrugs().isEmpty());
        createDrug();
        assertFalse(warehouseService.getAllDrugs().isEmpty());
    }

    @Test
    @Transactional
    void testGetAllDrugsWithPagination() {

        PagedResult<Drug> result = warehouseService.getAllDrugs(1, 10);

        assertNotNull(result);
        assertEquals(1, result.page());
        assertEquals(10, result.limit());
        assertEquals(0, result.totalItems());
        assertEquals(1, result.totalPages());
        assertTrue(result.items().isEmpty());

        createDrug();

        result = warehouseService.getAllDrugs(1, 10);

        assertNotNull(result);
        assertEquals(1, result.page());
        assertEquals(10, result.limit());
        assertEquals(1, result.totalItems());
        assertEquals(1, result.totalPages());
        assertFalse(result.items().isEmpty());
    }

    @Test
    @Transactional
    void testCreateTransfer() {
        Drug drug = createDrug();

        Transfer transfer = warehouseService.createTransfer(TransferType.IN, drug.id.intValue(), 10);

        assertNotNull(transfer);
        assertEquals(TransferType.IN, transfer.getType());
        assertEquals(drug.id, transfer.getDrug().id);
        assertEquals(10, transfer.getQuantity());
        assertNotNull(transfer.getTransferDate());
        assertEquals(110, transfer.getDrug().getStock());

        transfer = warehouseService.createTransfer(TransferType.OUT, drug.id.intValue(), 100);

        assertNotNull(transfer);
        assertEquals(TransferType.OUT, transfer.getType());
        assertEquals(drug.id, transfer.getDrug().id);
        assertEquals(100, transfer.getQuantity());
        assertNotNull(transfer.getTransferDate());
        assertEquals(10, transfer.getDrug().getStock());

        // drugId does not exist
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> warehouseService.createTransfer(TransferType.IN, 999, 10));
        assertEquals("Drug not found", illegalArgumentException.getMessage());

        // quantity is more than stock
        illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> warehouseService.createTransfer(TransferType.OUT, drug.id.intValue(), 1000));
        assertEquals("Insufficient stock", illegalArgumentException.getMessage());

        // transfer type is null
        jakarta.validation.ConstraintViolationException constraintViolationException = assertThrows(jakarta.validation.ConstraintViolationException.class, () -> warehouseService.createTransfer(null, drug.id.intValue(), 10));
        var violations = constraintViolationException.getConstraintViolations().stream().toList();
        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.getFirst().getMessage());
        assertEquals("createTransfer.type", violations.getFirst().getPropertyPath().toString());

        // drugId is negative
        constraintViolationException = assertThrows(jakarta.validation.ConstraintViolationException.class, () -> warehouseService.createTransfer(TransferType.IN, -1, 10));
        violations = constraintViolationException.getConstraintViolations().stream().toList();
        assertEquals(1, violations.size());
        assertEquals("must be greater than 0", violations.getFirst().getMessage());
        assertEquals("createTransfer.drugId", violations.getFirst().getPropertyPath().toString());

        // quantity is negative
        constraintViolationException = assertThrows(jakarta.validation.ConstraintViolationException.class, () -> warehouseService.createTransfer(TransferType.IN, drug.id.intValue(), -10));
        violations = constraintViolationException.getConstraintViolations().stream().toList();
        assertEquals(1, violations.size());
        assertEquals("must be greater than 0", violations.getFirst().getMessage());
        assertEquals("createTransfer.quantity", violations.getFirst().getPropertyPath().toString());
    }

    @Test
    @Transactional
    void testGetTransfers() {
        Drug drug = createDrug();

        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = Instant.now().plus(1, ChronoUnit.DAYS);

        // no transfers exist
        PagedResult<Transfer> result = warehouseService.getTransfers(1, 10, List.of(), from, to);

        assertNotNull(result);
        assertEquals(1, result.page());
        assertEquals(10, result.limit());
        assertEquals(0, result.totalItems());
        assertEquals(0, result.totalPages());
        assertTrue(result.items().isEmpty());

        // single transfer exists
        Transfer transfer = warehouseService.createTransfer(TransferType.IN, drug.id.intValue(), 10);

        result = warehouseService.getTransfers(1, 10, List.of(drug.id), from, to);

        assertNotNull(result);
        assertEquals(1, result.page());
        assertEquals(10, result.limit());
        assertEquals(1, result.totalItems());
        assertEquals(1, result.totalPages());
        assertFalse(result.items().isEmpty());
        assertEquals(transfer.id, result.items().getFirst().id);

        // to is null
        result = warehouseService.getTransfers(1, 10, List.of(drug.id), from, null);

        assertNotNull(result);
        assertEquals(1, result.page());
        assertEquals(10, result.limit());
        assertEquals(1, result.totalItems());
        assertEquals(1, result.totalPages());
        assertFalse(result.items().isEmpty());
        assertEquals(transfer.id, result.items().getFirst().id);

        // from is null
        result = warehouseService.getTransfers(1, 10, List.of(drug.id), null, to);

        assertNotNull(result);
        assertEquals(1, result.page());
        assertEquals(10, result.limit());
        assertEquals(1, result.totalItems());
        assertEquals(1, result.totalPages());
        assertFalse(result.items().isEmpty());
        assertEquals(transfer.id, result.items().getFirst().id);

        // from and to is null
        result = warehouseService.getTransfers(1, 10, List.of(drug.id), null, null);

        assertNotNull(result);
        assertEquals(1, result.page());
        assertEquals(10, result.limit());
        assertEquals(1, result.totalItems());
        assertEquals(1, result.totalPages());
        assertFalse(result.items().isEmpty());
        assertEquals(transfer.id, result.items().getFirst().id);

        // transfer exists but from/to is out of range
        result = warehouseService.getTransfers(1, 10, List.of(drug.id), from.minus(2, ChronoUnit.DAYS), to.minus(1, ChronoUnit.DAYS));

        assertNotNull(result);
        assertEquals(1, result.page());
        assertEquals(10, result.limit());
        assertEquals(0, result.totalItems());
        assertEquals(0, result.totalPages());
        assertTrue(result.items().isEmpty());

        // transfer exists but from/to is out of range
        result = warehouseService.getTransfers(1, 10, List.of(drug.id), from.plus(2, ChronoUnit.DAYS), to.plus(3, ChronoUnit.DAYS));

        assertNotNull(result);
        assertEquals(1, result.page());
        assertEquals(10, result.limit());
        assertEquals(0, result.totalItems());
        assertEquals(0, result.totalPages());
        assertTrue(result.items().isEmpty());

        // transfer exists, but drug id 999 does not, from and to are null
        result = warehouseService.getTransfers(1, 10, List.of(999L), null, null);

        assertNotNull(result);
        assertEquals(1, result.page());
        assertEquals(10, result.limit());
        assertEquals(0, result.totalItems());
        assertEquals(0, result.totalPages());
        assertTrue(result.items().isEmpty());

    }

}