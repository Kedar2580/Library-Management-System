package com.library.model;

public enum MessageStatus {
    NEW("New"),
    READ("Read"),
    RESOLVED("Resolved");

    private final String label;

    MessageStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
