package com.library.model;

public enum FineStatus {
    PENDING("Pending"),
    PAID("Paid");

    private final String label;

    FineStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
