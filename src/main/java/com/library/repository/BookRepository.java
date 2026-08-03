package com.library.repository;

import com.library.model.Book;
import com.library.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByTitleIgnoreCase(String title);

    List<Book> findByCategory(Category category);

    long countByAvailableCopiesGreaterThan(int copies);

    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(b.author.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(b.category.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(b.publisher.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Book> search(@Param("q") String q);

    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Book> searchByTitleOrIsbn(@Param("q") String q);

    @Query("SELECT b FROM Book b WHERE LOWER(b.author.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Book> searchByAuthor(@Param("q") String q);

    @Query("SELECT b FROM Book b WHERE LOWER(b.category.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Book> searchByCategory(@Param("q") String q);
}
