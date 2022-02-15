package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Builder
@AllArgsConstructor
@Getter
public class Sales {
    @Builder.Default
    private String productName = "";

    @Builder.Default
    private BigDecimal sellPrice = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);

    @Builder.Default
    private BigDecimal buyPrice = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);

    @Builder.Default
    private BigDecimal totalSales = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);

    private int quantity;

    public BigDecimal getTotalProfit(){
        BigDecimal totalCost = new BigDecimal(quantity).multiply(buyPrice);
        return totalSales.subtract(totalCost);
    }

    public BigDecimal profitPerUnit() {
        return getTotalProfit().divide(new BigDecimal(quantity), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal profitMargin() {
        return profitPerUnit().divide(totalSales, 2, RoundingMode.HALF_UP);
    }
}
