package gr.ots.pharmacy.warehouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

@MappedSuperclass
public class BaseEntity extends PanacheEntity {

    @NotNull
    @Schema(readOnly = true)
    @Column(name = "CREATED_AT", updatable = false)
    private Instant createdAt;

    @Schema(readOnly = true)
    @Column(insertable = false, name = "UPDATED_AT")
    private Instant updatedAt;

    @Version
    @PositiveOrZero
    @Column(nullable = false)
    private long version;

    @PrePersist
    private void prePersist() {
        this.createdAt = TruncateUtil.truncate(Instant.now());
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = TruncateUtil.truncate(Instant.now());
    }

    @AssertTrue(message = "Updated date must be after created date")
    private boolean isUpdateDateValid() {
        return updatedAt == null || (createdAt != null && updatedAt.isAfter(createdAt));
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
