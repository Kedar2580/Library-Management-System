package com.library.repository;

import com.library.model.BookIssue;
import com.library.model.IssueStatus;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookIssueRepository extends JpaRepository<BookIssue, Long> {

    List<BookIssue> findByMember(User member);

    List<BookIssue> findByMemberOrderByIdDesc(User member);

    List<BookIssue> findByStatusOrderByIdDesc(IssueStatus status);

    List<BookIssue> findByStatus(IssueStatus status);

    List<BookIssue> findByStatusAndDueDateBefore(IssueStatus status, LocalDate date);

    List<BookIssue> findByIssueDateBetween(LocalDate from, LocalDate to);

    List<BookIssue> findByReturnDateBetween(LocalDate from, LocalDate to);

    List<BookIssue> findByIssueDateBetweenAndStatus(LocalDate from, LocalDate to, IssueStatus status);

    List<BookIssue> findByReturnDateBetweenAndStatus(LocalDate from, LocalDate to, IssueStatus status);

    long countByStatus(IssueStatus status);

    long countByBookIdAndStatus(Long bookId, IssueStatus status);
}
