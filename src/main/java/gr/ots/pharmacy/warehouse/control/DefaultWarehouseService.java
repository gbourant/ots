package gr.ots.pharmacy.warehouse.control;

import gr.ots.pharmacy.warehouse.boundary.DrugCreateDTO;
import gr.ots.pharmacy.warehouse.entity.*;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Transactional
@ApplicationScoped
public class DefaultWarehouseService implements WarehouseService {

    @Override
    public Drug createDrug(DrugCreateDTO drugCreateDTO) {
        Drug drug = new Drug(drugCreateDTO);
        drug.persist();
        return drug;
    }

    @Override
    public List<Drug> getAllDrugs() {
        return Drug.listAll();
    }

    @Override
    public PagedResult<Drug> getAllDrugs(int page, int limit) {

        page = Math.max(1, page);
        limit = Math.max(1, Math.min(limit, 50));

        PanacheQuery<Drug> queryDrugs = Drug.findAll(Sort.by(Drug_.CREATED_AT).descending());
        PanacheQuery<Drug> paginatedQuery = queryDrugs.page(page - 1, limit);

        int totalPages = queryDrugs.pageCount();
        long totalDrugs = queryDrugs.count();

        return new PagedResult<>(page, limit, totalDrugs, totalPages, paginatedQuery.list());
    }

    @Override
    public Transfer createTransfer(@NotNull TransferType type, @Positive int drugId, @Positive int quantity) {
        Drug drug = Drug.findById(drugId);

        if (drug == null) {
            throw new IllegalArgumentException("Drug not found");
        }

        if (type == TransferType.OUT && drug.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        Transfer transfer = new Transfer();
        transfer.setType(type);
        transfer.setDrug(drug);
        transfer.setQuantity(quantity);
        transfer.setTransferDate(Instant.now());

        drug.setStock(drug.getStock() + (type == TransferType.IN ? quantity : -quantity));

        drug.persist();
        transfer.persist();

        return transfer;
    }

    @Override
    public PagedResult<Transfer> getTransfers(int page, int limit, List<Long> drugIds, Instant from, Instant to) {
        page = Math.max(1, page);
        limit = Math.max(1, Math.min(limit, 50));

        CriteriaBuilder cb = Transfer.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Transfer> cq = cb.createQuery(Transfer.class);
        Root<Transfer> root = cq.from(Transfer.class);

        List<Predicate> predicates = new ArrayList<>();

        if (drugIds != null && !drugIds.isEmpty()) {
            predicates.add(root.get(Transfer_.drug).get(Drug_.id).in(drugIds));
        }

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(Transfer_.transferDate), from));
        }

        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(Transfer_.transferDate), to));
        }

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        cq.orderBy(cb.desc(root.get(Transfer_.createdAt)));

        TypedQuery<Transfer> query = Transfer.getEntityManager().createQuery(cq);
        query.setFirstResult((page - 1) * limit);
        query.setMaxResults(limit);

        List<Transfer> transfers = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Transfer> countRoot = countQuery.from(Transfer.class);
        countQuery.select(cb.count(countRoot));
        if (!predicates.isEmpty()) {
            countQuery.where(predicates.toArray(new Predicate[0]));
        }

        Long count = Transfer.getEntityManager().createQuery(countQuery).getSingleResult();
        int totalPages = (int) Math.ceil((double) count / limit);

        return new PagedResult<>(page, limit, count, totalPages, transfers);
    }
}
