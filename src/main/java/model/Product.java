package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Builder
@Getter
@AllArgsConstructor
public class Product {

    private final String name;
    private BigDecimal retailPrice;
    private BigDecimal wholesalePrice;
    private BigDecimal drp;
    private int unitsPerCarton;
    private int currentStock;
    private int weeklyNeededStock;
    @Builder.Default
    private int requiredCartons = -1;
    private String discountCode;
    private String supplierID;
    private boolean isCarton;

    public Product(String name, BigDecimal price, BigDecimal drp, int unitsPerCarton, int currentStock, String discountCode, boolean isCarton) {
        this.name = name;
        this.retailPrice = price;
        this.drp = drp;
        this.unitsPerCarton = unitsPerCarton;
        this.currentStock = currentStock;
        if (discountCode == null || discountCode.isBlank()) {
            this.discountCode = "";
        }
        this.isCarton = isCarton;
    }

    public int getRequiredStock() {
        if (weeklyNeededStock > currentStock) {
            return weeklyNeededStock - currentStock;
        } else {
            return 0;
        }
    }

    /**
     * Returns the number of cartons needed to reach the product's required stock level. If the value has not been set to
     * a positive value, the amount will be calculated using carton size and current/required stock levels.
     * @return the number of cartons needed to reach the product's required stock level
     */
    public int getRequiredCartons() {
        if (requiredCartons >= 0) {
            return requiredCartons;
        } else if (unitsPerCarton == 0){
            return getRequiredStock();
        } else {
            int a = (int) Math.ceil(getRequiredStock() / (float) getUnitsPerCarton());
            return  a;
        }
    }

    public void setRequiredCartons(int requiredCartons) {
        this.requiredCartons = requiredCartons;
    }

    public void addStock(int amount) {
        currentStock = currentStock + amount;
    }

    public boolean isCarton() {
        return isCarton;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
