package gr.ots.pharmacy.warehouse.boundary;

import gr.ots.pharmacy.warehouse.control.PagedResult;
import gr.ots.pharmacy.warehouse.control.WarehouseService;
import gr.ots.pharmacy.warehouse.entity.Drug;
import gr.ots.pharmacy.warehouse.entity.Transfer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.time.Instant;
import java.util.List;

@Path("warehouse")
@ApplicationScoped
public class WarehouseResource {

    @Inject
    private WarehouseService warehouseService;

    @GET
    public PagedResult<Drug> getAllDrugs(@QueryParam("page") @DefaultValue("1") int page,
                                         @QueryParam("limit") @DefaultValue("10") int limit) {
        return warehouseService.getAllDrugs(page, limit);
    }

    @GET
    @Path("all")
    public List<Drug> getAllDrugs() {
        return warehouseService.getAllDrugs();
    }

    @POST
    public Drug createNewDrug(DrugCreateDTO drugCreateDTO) {
        return warehouseService.createDrug(drugCreateDTO);
    }

    @GET
    @Path("transfer")
    public PagedResult<Transfer> getTransfers(@QueryParam("page") @DefaultValue("1") int page,
                                              @QueryParam("limit") @DefaultValue("10") int limit,
                                              @QueryParam("drugIds") List<Long> drugIds,
                                              @QueryParam("from") Instant from,
                                              @QueryParam("to") Instant to) {
        return warehouseService.getTransfers(page, limit, drugIds, from, to);
    }

    @POST
    @Path("transfer")
    public Transfer createTransfer(TransferCreateDTO transferCreateDTO) {
        return warehouseService.createTransfer(transferCreateDTO.type(), transferCreateDTO.drugId(), transferCreateDTO.quantity());
    }

}
