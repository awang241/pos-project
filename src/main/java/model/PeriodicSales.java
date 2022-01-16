package model;

import enums.Period;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PeriodicSales {
    @Getter @Setter
    private Period period;
    @Getter @Setter
    private String productName;
    @Getter
    private final Map<Integer, Integer> quantity = new HashMap<>();

    public PeriodicSales(String name, Map<Integer, Integer> sales, Period period) {
        this.productName = name;
        this.period = period;
        for (Map.Entry<Integer, Integer> sale: sales.entrySet()) {
            int index = sale.getKey();
            if (period.equals(Period.MONTHLY) && (index < 1 || index > 12)) {
                throw new IllegalArgumentException("Invalid index for monthly sales");
            } else if (period.equals(Period.WEEKLY) && (index < 1 || index > 53)) {
                throw new IllegalArgumentException("Invalid index for weekly sales");
            } else {
                quantity.put(index, sale.getValue());
            }
        }
    }

    public void setQuantity(int index, int quantity) {
        this.quantity.put(index, quantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeriodicSales that = (PeriodicSales) o;
        return period == that.period && Objects.equals(productName, that.productName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(period, productName);
    }
}
