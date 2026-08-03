package com.library.repository;

import com.library.model.Book;
import com.library.model.CartItem;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByMember(User member);

    Optional<CartItem> findByMemberAndBook(User member, Book book);

    long countByMember(User member);

    void deleteByMember(User member);
}
