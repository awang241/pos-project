package model;

import enums.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Transaction {

    private final Set<TransactionItem> items;
    @Getter @Setter
    private LocalDateTime dateTime;
    @Getter @Setter
    private BigDecimal payment;
    @Getter
    private final PaymentType type;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Transaction(Collection<TransactionItem> items, LocalDateTime dateTime, PaymentType type) {
        this.items = new HashSet<>(items);
        this.dateTime = dateTime;
        this.type = type;
        this.payment = this.getTotal();
    }

    public Transaction(Collection<TransactionItem> items, LocalDateTime dateTime, PaymentType type, BigDecimal payment) {
        this.items = new HashSet<>(items);
        this.dateTime = dateTime;
        this.type = type;
        this.payment = payment;
    }

    public String getDateString() {
        return dateTime.format(dateFormatter);
    }

    public Set<TransactionItem> getItems() {
        return Collections.unmodifiableSet(items);
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(item -> (item.getPrice().multiply(new BigDecimal(item.getQuantity()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isComplete() {
        return items != null;
    }
}
