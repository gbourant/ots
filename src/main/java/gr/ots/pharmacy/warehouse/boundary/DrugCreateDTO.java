package gr.ots.pharmacy.warehouse.boundary;

import java.math.BigDecimal;

public record DrugCreateDTO(String name, String code, BigDecimal price, int stock, int categoryId) {
}
