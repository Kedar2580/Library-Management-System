package com.library.model;

public enum Role {
    ADMIN("Admin"),
    LIBRARIAN("Librarian"),
    MEMBER("Member");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
