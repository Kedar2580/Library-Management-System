package com.library.model;

public enum MessageType {
    FEEDBACK("Feedback"),
    SUGGESTION("Suggestion"),
    QUESTION("Question");

    private final String label;

    MessageType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
