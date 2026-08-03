package com.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String action;

    private String username;

    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void onPersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
