package gr.ots.pharmacy.warehouse.control;


import gr.ots.pharmacy.warehouse.boundary.DrugCreateDTO;
import gr.ots.pharmacy.warehouse.entity.Drug;
import gr.ots.pharmacy.warehouse.entity.Transfer;
import gr.ots.pharmacy.warehouse.entity.TransferType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;

public interface WarehouseService {

    // Τη δημιουργία νέων φαρμάκων
    Drug createDrug(DrugCreateDTO drugCreateDTO);

    // Την προβολή όλων των φαρμάκων της αποθήκης
    List<Drug> getAllDrugs();

    // Την προβολή όλων των φαρμάκων της αποθήκης με pagination
    PagedResult<Drug> getAllDrugs(int page, int limit);

    // Τη δημιουργία κινήσεων στην αποθήκη (εισαγωγή/εξαγωγή) και ενημέρωση των αποθεμάτων
    Transfer createTransfer(@NotNull TransferType type, @Positive int drugId, @Positive int quantity);

    // Την προβολή των κινήσεων εξαγωγής της αποθήκης για συγκεκριμένα φάρμακα και συγκεκριμένο εύρος ημερομηνιών.
    PagedResult<Transfer> getTransfers(int page, int limit, List<Long> drugIds, Instant from, Instant to);

}
