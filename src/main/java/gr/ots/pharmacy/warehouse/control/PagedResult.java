package gr.ots.pharmacy.warehouse.control;

import java.util.List;

public record PagedResult<T>(int page, int limit, long totalItems, int totalPages, List<T> items) {
}
