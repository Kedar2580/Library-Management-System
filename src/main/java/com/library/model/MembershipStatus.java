package com.library.model;

public enum MembershipStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended");

    private final String label;

    MembershipStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
