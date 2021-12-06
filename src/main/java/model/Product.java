package model;

import java.util.Objects;

public class Product {
    private final String name;
    private float price;
    private float drp;
    private int unit;
    private int stockLevel;
    private boolean isCarton;

    public Product(String name, float price, float drp, int unit, int stockLevel, boolean isCarton) {
        this.name = name;
        this.price = price;
        this.drp = drp;
        this.unit = unit;
        this.stockLevel = stockLevel;
        this.isCarton = isCarton;
    }

    public static Product createCashProduct(float amount) {
        return new Product("Cash Out", amount, amount, 0, 0, false);
    }

    public static Product createUncodedProduct(int uncodedCount, float price) {
        return new Product(String.format("Uncoded Product %d", uncodedCount), price, price, 0, 0, false);
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public float getDrp() {
        return drp;
    }

    public void setDrp(float drp) {
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
