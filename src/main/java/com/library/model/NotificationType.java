package com.library.model;

public enum NotificationType {
    DUE_DATE("Due date reminder"),
    OVERDUE("Overdue"),
    RESERVATION("Reservation"),
    FINE("Fine"),
    SYSTEM("System");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
