package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderItem {
    public int quantity;
    public long orderID;
    public BigDecimal price;
    public String product;
}
