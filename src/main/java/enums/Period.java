package enums;

public enum Period {
    MONTHLY("Monthly"), WEEKLY("Weekly");

    private final String name;
    Period(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }
}
