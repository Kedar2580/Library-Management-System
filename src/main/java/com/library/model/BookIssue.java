package com.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status = IssueStatus.ISSUED;

    private double fineAmount;

    @Column(length = 1000)
    private String notes;

    public long overdueDays() {
        if (status == IssueStatus.RETURNED || returnDate != null) {
            long days = ChronoUnit.DAYS.between(dueDate, returnDate);
            return Math.max(0, days);
        }
        long days = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return Math.max(0, days);
    }

    public boolean isOverdue() {
        return status != IssueStatus.RETURNED && dueDate.isBefore(LocalDate.now());
    }
}
