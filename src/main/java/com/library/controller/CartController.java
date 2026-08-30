package com.library.controller;

import com.library.model.Book;
import com.library.model.CartItem;
import com.library.model.Role;
import com.library.model.User;
import com.library.security.SecurityUtil;
import com.library.service.BookService;
import com.library.service.CartService;
import com.library.service.CirculationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/my/cart")
public class CartController {

    private final CartService cartService;
    private final BookService bookService;
    private final CirculationService circulationService;

    public CartController(CartService cartService, BookService bookService,
                          CirculationService circulationService) {
        this.cartService = cartService;
        this.bookService = bookService;
        this.circulationService = circulationService;
    }

    @GetMapping
    public String cart(Model model) {
        User member = currentMember();
        if (member == null) {
            return "redirect:/auth/member-login";
        }
        List<Book> books = cartService.items(member).stream()
                .map(CartItem::getBook)
                .toList();
        model.addAttribute("cartBooks", books);
        model.addAttribute("maxCartItems", CartService.MAX_CART_ITEMS);
        return "my/cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long bookId, HttpServletRequest request, RedirectAttributes ra) {
        User member = currentMember();
        if (member == null) {
            return "redirect:/auth/member-login";
        }
        Book book = bookService.getBook(bookId);
        if (book == null || !book.isAvailable()) {
            ra.addFlashAttribute("error", "This book is not available to borrow.");
            return "redirect:/my/books";
        }
        if (cartService.isFull(member)) {
            ra.addFlashAttribute("error", "Cart is full. You can add up to "
                    + CartService.MAX_CART_ITEMS + " books at a time. Remove one to add another.");
            return samePageRedirect(request, "/my/books");
        }
        if (cartService.add(member, book)) {
            ra.addFlashAttribute("success", "Book added to cart. Ready to checkout?");
        } else {
            ra.addFlashAttribute("error", "This book is already in your cart.");
        }
        return samePageRedirect(request, "/my/books");
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long bookId, RedirectAttributes ra) {
        User member = currentMember();
        if (member == null) {
            return "redirect:/auth/member-login";
        }
        cartService.remove(member, bookId);
        ra.addFlashAttribute("success", "Book removed from cart.");
        return "redirect:/my/cart";
    }

    @PostMapping("/clear")
    public String clear(RedirectAttributes ra) {
        User member = currentMember();
        if (member == null) {
            return "redirect:/auth/member-login";
        }
        cartService.clear(member);
        ra.addFlashAttribute("success", "Cart cleared.");
        return "redirect:/my/cart";
    }

    @PostMapping("/checkout")
    public String checkout(RedirectAttributes ra) {
        User member = currentMember();
        if (member == null) {
            return "redirect:/auth/member-login";
        }
        List<Long> ids = cartService.bookIds(member);
        if (ids.isEmpty()) {
            ra.addFlashAttribute("error", "Your cart is empty.");
            return "redirect:/my/cart";
        }
        List<String> errors = new ArrayList<>();
        int borrowed = 0;
        for (Long id : ids) {
            String error = circulationService.issueBook(id, member.getId());
            if (error != null) {
                errors.add(error);
            } else {
                borrowed++;
            }
        }
        cartService.clear(member);
        if (borrowed > 0) {
            ra.addFlashAttribute("success", borrowed + " book(s) borrowed successfully. Thank you!");
        }
        if (!errors.isEmpty()) {
            ra.addFlashAttribute("error", String.join(" ", errors.stream().distinct().toList()));
        }
        return "redirect:/my/books";
    }

    private User currentMember() {
        User u = SecurityUtil.currentUser();
        return u != null && u.getRole() == Role.MEMBER ? u : null;
    }

    private String samePageRedirect(HttpServletRequest request, String fallback) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            String base = request.getRequestURL().toString()
                    .replace(request.getRequestURI(), "");
            if (referer.startsWith(base)) {
                return "redirect:" + referer;
            }
        }
        return "redirect:" + fallback;
    }
}
