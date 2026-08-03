package com.library.service;

import com.library.model.Book;
import com.library.model.CartItem;
import com.library.model.User;
import com.library.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;

    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<CartItem> items(User member) {
        return cartItemRepository.findByMember(member);
    }

    public List<Long> bookIds(User member) {
        return items(member).stream().map(item -> item.getBook().getId()).toList();
    }

    public int size(User member) {
        return (int) cartItemRepository.countByMember(member);
    }

    @Transactional
    public boolean add(User member, Book book) {
        if (cartItemRepository.findByMemberAndBook(member, book).isPresent()) {
            return false;
        }
        CartItem item = new CartItem();
        item.setMember(member);
        item.setBook(book);
        cartItemRepository.save(item);
        return true;
    }

    @Transactional
    public void remove(User member, Long bookId) {
        List<CartItem> items = cartItemRepository.findByMember(member);
        items.stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst()
                .ifPresent(cartItemRepository::delete);
    }

    @Transactional
    public void clear(User member) {
        cartItemRepository.deleteByMember(member);
    }
}
