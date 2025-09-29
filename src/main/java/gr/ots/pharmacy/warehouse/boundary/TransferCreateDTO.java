package gr.ots.pharmacy.warehouse.boundary;

import gr.ots.pharmacy.warehouse.entity.TransferType;

public record TransferCreateDTO(TransferType type, int drugId, int quantity) {
}
