package com.library.repository;

import com.library.model.Fine;
import com.library.model.FineStatus;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {

    List<Fine> findByStatusOrderByCreatedAtDesc(FineStatus status);

    List<Fine> findByMemberOrderByCreatedAtDesc(User member);

    List<Fine> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByStatus(FineStatus status);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.status = :status")
    double sumAmountByStatus(@Param("status") FineStatus status);
}
