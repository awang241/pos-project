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

    /**
     * Returns a payment type that matches the given string. If none match, returns null.
     * @param s The name of the payment type.
     * @return The corresponding PaymentType, or null if none match
     */
    public static PaymentType fromString(String s) {
        for (PaymentType type: PaymentType.values()) {
            if (type.toString().equalsIgnoreCase(s)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
