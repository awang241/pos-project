package enums;

public enum PaymentType {
    CASH("Cash"),
    EFTPOS("EFTPOS"),
    CHEQUE("Cheque"),
    NONE(""),
    REFUND("REFUND");

    private String name;

    PaymentType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
