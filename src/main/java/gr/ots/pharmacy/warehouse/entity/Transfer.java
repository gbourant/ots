package gr.ots.pharmacy.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;

@Entity
public class Transfer extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransferType type;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "DRUG_ID")
    private Drug drug;

    @PositiveOrZero
    private int quantity;

    @NotNull
    @Column(name = "TRANSFER_DATE")
    private Instant transferDate;

    public TransferType getType() {
        return type;
    }

    public void setType(TransferType type) {
        this.type = type;
    }

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Instant getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(Instant transferDate) {
        this.transferDate = transferDate;
    }
}
