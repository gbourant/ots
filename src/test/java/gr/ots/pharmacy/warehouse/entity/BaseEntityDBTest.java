package gr.ots.pharmacy.warehouse.entity;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class BaseEntityDBTest {

    @Entity
    @Table(name = "BASE_ENTITY")
    static public class MyEntity extends BaseEntity {
        public String name;

        public Instant instant;
    }

    @Test
    public void testBaseEntity() {
        final Instant immutableInstant = TruncateUtil.truncate(Instant.parse("2025-09-28T09:30:10.413315Z"));
        final Instant beforePersist = TruncateUtil.truncate(Instant.parse("2025-09-28T09:30:10.413677Z"));

        QuarkusTransaction.begin();
        MyEntity myEntity = new MyEntity();
        myEntity.name = "MyName";
        myEntity.instant = immutableInstant;
        myEntity.persist();
        QuarkusTransaction.commit();

        // Run it on another transaction, so myEntity and savedEntity are different instance
        QuarkusTransaction.begin();

        MyEntity savedEntity = MyEntity.findById(1L);

        assertNotNull(savedEntity);
        assertNotSame(savedEntity, myEntity);

        // Check that the myEntity saved as expected
        assertEquals(1L, savedEntity.id);
        assertEquals(0, savedEntity.getVersion());
        assertEquals("MyName", savedEntity.name);

        assertNotNull(savedEntity.instant);
        assertNotSame(immutableInstant, savedEntity.instant);
        assertEquals(immutableInstant, savedEntity.instant);

        assertNotNull(savedEntity.getCreatedAt());
        assertTrue(savedEntity.getCreatedAt().isAfter(beforePersist));
        assertNotSame(myEntity.getCreatedAt(), savedEntity.getCreatedAt());
        assertEquals(myEntity.getCreatedAt(), savedEntity.getCreatedAt());

        assertNull(savedEntity.getUpdatedAt());

        savedEntity.name = "YourName";
        savedEntity.persistAndFlush();

        // On update, check that only version, name and updatedAt are changed

        assertEquals(1L, savedEntity.id);
        assertEquals(1L, savedEntity.getVersion());
        assertEquals("YourName", savedEntity.name);
        assertEquals(myEntity.getCreatedAt(), savedEntity.getCreatedAt());
        assertEquals(immutableInstant, savedEntity.instant);

        assertNotNull(savedEntity.getUpdatedAt());
        assertTrue(savedEntity.getUpdatedAt().isAfter(beforePersist));
        assertNotSame(myEntity.getUpdatedAt(), savedEntity.getUpdatedAt());
        assertTrue(savedEntity.getUpdatedAt().isAfter(savedEntity.getCreatedAt()));

        QuarkusTransaction.commit();
    }

}
