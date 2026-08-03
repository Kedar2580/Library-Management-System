package com.library.model;

public enum PaymentMethod {
    CASH("Cash"),
    CARD("Card"),
    ONLINE("Online"),
    OTHER("Other");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
