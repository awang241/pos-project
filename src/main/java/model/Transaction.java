package model;

import enums.PaymentType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

public class Transaction {

    private Map<Product, Integer> items;
    private LocalDateTime dateTime;
    private PaymentType type;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Transaction(Map<Product, Integer> items, LocalDateTime dateTime, PaymentType type) {
        this.items = items;
        this.dateTime = dateTime;
        this.type = type;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getDateString() {
        return dateTime.format(dateFormatter);
    }

    public Map<Product, Integer> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public float getTotal() {
        float total = 0f;
        for (Map.Entry<Product, Integer> item: items.entrySet()) {
            boolean discounted = (item.getValue() >= item.getKey().getUnit()) && (item.getKey().getDrp() > 0);
            float price = discounted ? item.getKey().getDrp(): item.getKey().getPrice();
            total += price * item.getValue();
        }
        return total;
    }

    public PaymentType getType() {
        return type;
    }

    public boolean isComplete() {
        return items != null;
    }

    public boolean isDiscounted(Product product) {
        if (!items.containsKey(product) || product.getDrp() <= 0f) {
            return false;
        } else {
            return items.get(product) >= product.getUnit();
        }
    }
}
