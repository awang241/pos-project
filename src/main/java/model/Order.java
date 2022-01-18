package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class Order {

    public static final String ALL_SUPPLIERS = "All Suppliers";

    private LocalDate orderDate;
    private LocalDate paymentDate;
    private LocalDate deliveryDate;
    private String supplierCode;
    private List<Product> items;

    public Order(LocalDate orderDate, LocalDate paymentDate, LocalDate deliveryDate, String supplierCode) {
        this.orderDate = orderDate;
        this.paymentDate = paymentDate;
        this.deliveryDate = deliveryDate;
        this.supplierCode = supplierCode;
        items = new ArrayList<>();
    }

    public List<Product> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItems(Collection<Product> items) {
        this.items.addAll(items);
    }

    public void addItems(Product item) {
        this.items.add(item);
    }

    public void clearItems() {
        this.items.clear();
    }
}
