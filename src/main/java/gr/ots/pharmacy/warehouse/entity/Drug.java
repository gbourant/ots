package gr.ots.pharmacy.warehouse.entity;

import gr.ots.pharmacy.warehouse.boundary.DrugCreateDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;

@Entity
public class Drug extends BaseEntity {

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Column(unique = true)
    @Size(min = 2, max = 50)
    private String code;

    @NotNull
    @PositiveOrZero
    private BigDecimal price;

    @PositiveOrZero
    private int stock;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public Drug() {
    }

    public Drug(DrugCreateDTO drugCreateDTO) {
        this.name = drugCreateDTO.name();
        this.code = drugCreateDTO.code();
        this.price = drugCreateDTO.price();
        this.stock = drugCreateDTO.stock();
        this.category = Category.findById(drugCreateDTO.categoryId());
        if (this.category == null) {
            throw new NotFoundException("Category not found");
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
