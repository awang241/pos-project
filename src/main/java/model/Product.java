package model;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {

    private final String name;
    private BigDecimal price;
    private BigDecimal drp;
    private int unit;
    private int stockLevel;
    private String discountCode;
    private boolean isCarton;

    public Product(String name, BigDecimal price, BigDecimal drp, int unit, int stockLevel, String discountCode, boolean isCarton) {
        this.name = name;
        this.price = price;
        this.drp = drp;
        this.unit = unit;
        this.stockLevel = stockLevel;
        if (discountCode == null || discountCode.isBlank()) {
            this.discountCode = "";
        }
        this.isCarton = isCarton;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public BigDecimal getDrp() {
        return drp;
    }

    public void setDrp(BigDecimal drp) {
        this.drp = drp;
    }

    public void addStock(int amount) {
        stockLevel = stockLevel + amount;
    }

    public int getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(int stockLevel) {
        this.stockLevel = stockLevel;
    }

    public boolean isCarton() {
        return isCarton;
    }

    public void setCarton(boolean carton) {
        isCarton = carton;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
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
