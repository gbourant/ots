package gr.ots.pharmacy.warehouse.boundary;

import gr.ots.pharmacy.warehouse.entity.TransferType;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class WarehouseResourceTest {

    @Inject
    Flyway flyway;

    @BeforeEach
    void clean() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void createNewDrug() {
        DrugCreateDTO drug = new DrugCreateDTO("Aspirin", "ASP100", BigDecimal.valueOf(9.99), 100, 42);

        given()
                .contentType(ContentType.JSON)
                .body(drug)
                .when().post("/warehouse")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", is("Aspirin"))
                .body("code", is("ASP100"))
                .body("price", is(9.99F))
                .body("stock", is(100))
                .body("category.id", is(42));

        given()
                .contentType(ContentType.JSON)
                .body(drug)
                .when().post("/warehouse")
                .then()
                .statusCode(400)
                .body("violations[0].message", is("Duplication found"));

        drug = new DrugCreateDTO(null, "ASP100", BigDecimal.valueOf(9.99), 100, 42);

        given()
                .contentType(ContentType.JSON)
                .body(drug)
                .when().post("/warehouse")
                .then()
                .statusCode(400)
                .body("violations[0].path", is("name"))
                .body("violations[0].message", is("must not be blank"));


        drug = new DrugCreateDTO(null, "ASP200", BigDecimal.valueOf(9.99), 100, 900);

        given()
                .contentType(ContentType.JSON)
                .body(drug)
                .when().post("/warehouse")
                .then()
                .statusCode(404)
                .body("error", is("Category not found"));
    }

    @Test
    void getAllDrugs() {
        given()
                .when().get("/warehouse/all")
                .then()
                .statusCode(200)
                .body("size()", is(0));

        this.createNewDrug();

        given()
                .when().get("/warehouse/all")
                .then()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void testGetAllDrugsPaginated() {
        for (int i = 0; i < 15; i++) {
            DrugCreateDTO drug = new DrugCreateDTO("Drug" + i, "CODE" + i, BigDecimal.valueOf(9.99), 100, 42);
            given()
                    .contentType(ContentType.JSON)
                    .body(drug)
                    .when().post("/warehouse")
                    .then()
                    .statusCode(200);
        }

        // first page with default values
        given()
                .when().get("/warehouse")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(10))
                .body("totalItems", is(15))
                .body("totalPages", is(2))
                .body("items.size()", is(10));

        // second page
        given()
                .when().get("/warehouse?page=2")
                .then()
                .statusCode(200)
                .body("page", is(2))
                .body("limit", is(10))
                .body("totalItems", is(15))
                .body("totalPages", is(2))
                .body("items.size()", is(5));

        // custom limit
        given()
                .when().get("/warehouse?limit=5")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(5))
                .body("totalItems", is(15))
                .body("totalPages", is(3))
                .body("items.size()", is(5));
    }

    @Test
    void getTransfers() {

        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = Instant.now().plus(1, ChronoUnit.DAYS);

        // no transfers exist
        given()
                .when().get("/warehouse/transfer")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(10))
                .body("totalItems", is(0))
                .body("totalPages", is(0))
                .body("items.size()", is(0));

        DrugCreateDTO drug = new DrugCreateDTO("Aspirin", "ASP100", BigDecimal.valueOf(9.99), 100, 42);
        var drugResponse = given()
                .contentType(ContentType.JSON)
                .body(drug)
                .when().post("/warehouse")
                .then()
                .statusCode(200)
                .extract().jsonPath();

        TransferCreateDTO transfer = new TransferCreateDTO(TransferType.IN, drugResponse.getInt("id"), 10);
        given()
                .contentType(ContentType.JSON)
                .body(transfer)
                .when().post("/warehouse/transfer")
                .then()
                .statusCode(200);

        // transfer exists without any filter
        // from and to is not sent
        given()
                .when().get("/warehouse/transfer")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(10))
                .body("totalItems", is(1))
                .body("totalPages", is(1))
                .body("items.size()", is(1));

        // drug id exists
        given()
                .queryParam("drugIds", drugResponse.getInt("id"))
                .when().get("/warehouse/transfer")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(10))
                .body("totalItems", is(1))
                .body("totalPages", is(1))
                .body("items.size()", is(1));

        // to is not sent
        given()
                .queryParam("from", from.toString())
                .when().get("/warehouse/transfer")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(10))
                .body("totalItems", is(1))
                .body("totalPages", is(1))
                .body("items.size()", is(1));

        // from is not sent
        given()
                .queryParam("to", to.toString())
                .when().get("/warehouse/transfer")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(10))
                .body("totalItems", is(1))
                .body("totalPages", is(1))
                .body("items.size()", is(1));

        // transfer exists but from/to is out of range
        given()
                .queryParam("from", from.minus(2, ChronoUnit.DAYS).toString())
                .queryParam("to", to.minus(1, ChronoUnit.DAYS).toString())
                .when().get("/warehouse/transfer")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(10))
                .body("totalItems", is(0))
                .body("totalPages", is(0))
                .body("items.size()", is(0));

        // transfer exists but from/to is out of range
        given()
                .queryParam("from", from.plus(2, ChronoUnit.DAYS).toString())
                .queryParam("to", to.plus(3, ChronoUnit.DAYS).toString())
                .when().get("/warehouse/transfer")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(10))
                .body("totalItems", is(0))
                .body("totalPages", is(0))
                .body("items.size()", is(0));

        // transfer exists but drug id 999 does not
        given()
                .queryParam("drugIds", 999)
                .when().get("/warehouse/transfer")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(10))
                .body("totalItems", is(0))
                .body("totalPages", is(0))
                .body("items.size()", is(0));

        // drug id exists but from/to is our of range
        given()
                .queryParam("drugIds", drugResponse.getInt("id"))
                .queryParam("from", from.plus(2, ChronoUnit.DAYS).toString())
                .queryParam("to", to.plus(3, ChronoUnit.DAYS).toString())
                .when().get("/warehouse/transfer")
                .then()
                .statusCode(200)
                .body("page", is(1))
                .body("limit", is(10))
                .body("totalItems", is(0))
                .body("totalPages", is(0))
                .body("items.size()", is(0));
    }

    @Test
    void createTransfer() {
        DrugCreateDTO drug = new DrugCreateDTO("Aspirin", "ASP100", BigDecimal.valueOf(9.99), 100, 42);
        var drugResponse = given()
                .contentType(ContentType.JSON)
                .body(drug)
                .when().post("/warehouse")
                .then()
                .statusCode(200)
                .extract().jsonPath();

        TransferCreateDTO transfer = new TransferCreateDTO(TransferType.IN, drugResponse.getInt("id"), 10);

        // successful create transfer
        given()
                .contentType(ContentType.JSON)
                .body(transfer)
                .when().post("/warehouse/transfer")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("drug.id", is(drugResponse.getInt("id")))
                .body("transferDate", notNullValue())
                .body("quantity", is(10))
                .body("type", is("IN"));

        // invalid drug id
        transfer = new TransferCreateDTO(TransferType.IN, 999, 10);
        given()
                .contentType(ContentType.JSON)
                .body(transfer)
                .when().post("/warehouse/transfer")
                .then()
                .statusCode(400)
                .body("violations[0].message", is("Drug not found"));

        // insufficient stock
        transfer = new TransferCreateDTO(TransferType.OUT, drugResponse.getInt("id"), 999);
        given()
                .contentType(ContentType.JSON)
                .body(transfer)
                .when().post("/warehouse/transfer")
                .then()
                .statusCode(400)
                .body("violations[0].message", is("Insufficient stock"));

        // type is null
        transfer = new TransferCreateDTO(null, 1, 999);
        given()
                .contentType(ContentType.JSON)
                .body(transfer)
                .when().post("/warehouse/transfer")
                .then()
                .statusCode(400)
                .body("violations[0].message", is("must not be null"))
                .body("violations[0].path", is("createTransfer.type"));

        // id is negative
        transfer = new TransferCreateDTO(TransferType.IN, -1, 999);
        given()
                .contentType(ContentType.JSON)
                .body(transfer)
                .when().post("/warehouse/transfer")
                .then()
                .statusCode(400)
                .body("violations[0].message", is("must be greater than 0"))
                .body("violations[0].path", is("createTransfer.drugId"));

        // id is negative
        transfer = new TransferCreateDTO(TransferType.IN, 1, -1);
        given()
                .contentType(ContentType.JSON)
                .body(transfer)
                .when().post("/warehouse/transfer")
                .then()
                .statusCode(400)
                .body("violations[0].message", is("must be greater than 0"))
                .body("violations[0].path", is("createTransfer.quantity"));
    }

}