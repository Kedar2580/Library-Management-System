package com.library.model;

public enum ReservationStatus {
    PENDING("Pending"),
    CANCELLED("Cancelled"),
    FULFILLED("Fulfilled");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
