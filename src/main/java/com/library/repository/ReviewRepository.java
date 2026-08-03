package com.library.repository;

import com.library.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookIdOrderByCreatedAtDesc(Long bookId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByBookId(Long bookId);
}
