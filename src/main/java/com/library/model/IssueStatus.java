package com.library.model;

public enum IssueStatus {
    ISSUED("Issued"),
    RETURNED("Returned"),
    OVERDUE("Overdue");

    private final String label;

    IssueStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
