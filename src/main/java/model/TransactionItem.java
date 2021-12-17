package model;

import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

public class TransactionItem {
    public static final String CASH_OUT = "Cash Out";
    public static final String UNCODED = "Uncoded Product ";

    @Getter
    private final long id;
    @Getter @Setter
    private Transaction transaction;
    @Getter @Setter
    private String productName;
    private IntegerProperty quantity;
    @Getter @Setter
    private String discountCode;
    private ObjectProperty<BigDecimal> price;

    public TransactionItem(long id, String name, int quantity, String discountCode, BigDecimal price) {
        this.id = id;
        this.productName = name;
        this.quantity = new SimpleIntegerProperty(quantity);
        this.discountCode = discountCode;
        this.price = new SimpleObjectProperty<>(price);
    }

    /**
     * Creates a new TransactionItem based on a given quantity plus the name, price, and discount code of the given product.
     * @param product The product the item is based on
     * @param quantity The quantity of the item
     */
    public TransactionItem(long id, Product product, int quantity) {
        this(id, product.getName(), quantity, product.getDiscountCode(), product.getPrice());
    }

    public static TransactionItem createCashProduct(BigDecimal amount) {
        return new TransactionItem(0, CASH_OUT, 1, null, amount);
    }

    public static TransactionItem createUncodedProduct(int index, BigDecimal price) {
        return new TransactionItem(-1 * index, String.format(UNCODED + "%d", index), 1, null, price);
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.setValue(quantity);
    }

    public BigDecimal getPrice() {
        return price.get();
    }

    public void setPrice(BigDecimal price) {
        this.price.setValue(price);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public ObjectProperty<BigDecimal> priceProperty() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionItem that = (TransactionItem) o;
        return id == that.id && quantity == that.quantity && this.price.equals(that.getPrice()) && Objects.equals(transaction, that.transaction) && productName.equals(that.productName) && Objects.equals(discountCode, that.discountCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
