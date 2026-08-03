package com.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String membershipNo;

    @Enumerated(EnumType.STRING)
    private MembershipStatus membershipStatus = MembershipStatus.ACTIVE;

    private LocalDate joinedDate = LocalDate.now();

    @Column(nullable = false)
    private boolean enabled = true;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void onPersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (joinedDate == null) {
            joinedDate = LocalDate.now();
        }
        if (membershipStatus == null) {
            membershipStatus = MembershipStatus.ACTIVE;
        }
    }
}
