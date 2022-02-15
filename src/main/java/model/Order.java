package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class Order {

    public static final String ALL_SUPPLIERS = "All Suppliers";

    @Builder.Default
    private long id = -1;
    private LocalDate orderDate;
    private LocalDate paymentDate;
    private LocalDate deliveryDate;
    private String supplierCode;
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public Order(LocalDate orderDate, LocalDate paymentDate, LocalDate deliveryDate, String supplierCode) {
        this.orderDate = orderDate;
        this.paymentDate = paymentDate;
        this.deliveryDate = deliveryDate;
        this.supplierCode = supplierCode;
        items = new ArrayList<>();
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItems(Collection<OrderItem> items) {
        this.items.addAll(items);
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    public void clearItems() {
        this.items.clear();
    }
}
