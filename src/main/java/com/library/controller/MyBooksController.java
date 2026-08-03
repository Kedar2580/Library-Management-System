package com.library.controller;

import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.model.Role;
import com.library.model.User;
import com.library.security.SecurityUtil;
import com.library.service.BookService;
import com.library.service.CirculationService;
import com.library.service.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/my/books")
public class MyBooksController {

    private final BookService bookService;
    private final CirculationService circulationService;
    private final SettingsService settingsService;

    public MyBooksController(BookService bookService, CirculationService circulationService,
                             SettingsService settingsService) {
        this.bookService = bookService;
        this.circulationService = circulationService;
        this.settingsService = settingsService;
    }

    @GetMapping
    public String books(Model model) {
        User member = currentMember();
        if (member == null) {
            return "redirect:/auth/member-login";
        }
        List<Book> available = bookService.allBooks().stream()
                .filter(Book::isAvailable)
                .toList();
        List<Book> unavailable = bookService.allBooks().stream()
                .filter(b -> !b.isAvailable())
                .toList();
        model.addAttribute("availableBooks", available);
        model.addAttribute("unavailableBooks", unavailable);
        model.addAttribute("myIssues", circulationService.activeIssuesForMember(member));
        List<Reservation> allReservations = circulationService.memberReservations(member);
        model.addAttribute("pendingReservations", allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .toList());
        model.addAttribute("maxBooks", settingsService.getInt("maxBooksPerMember", 5));
        return "my/books";
    }

    @PostMapping("/{bookId}/borrow")
    public String borrow(@PathVariable Long bookId, RedirectAttributes ra) {
        User member = currentMember();
        if (member == null) {
            return "redirect:/auth/member-login";
        }
        String error = circulationService.issueBook(bookId, member.getId());
        if (error != null) {
            ra.addFlashAttribute("error", error);
        } else {
            ra.addFlashAttribute("success", "Book borrowed successfully. Thank you!");
        }
        return "redirect:/my/books";
    }

    @PostMapping("/{bookId}/reserve")
    public String reserve(@PathVariable Long bookId, RedirectAttributes ra) {
        User member = currentMember();
        if (member == null) {
            return "redirect:/auth/member-login";
        }
        String error = circulationService.reserveBook(bookId, member.getId());
        if (error != null) {
            ra.addFlashAttribute("error", error);
        } else {
            ra.addFlashAttribute("success", "Book reserved. You will be notified when it is available.");
        }
        return "redirect:/my/books";
    }

    @PostMapping("/reservation/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, RedirectAttributes ra) {
        circulationService.cancelReservation(id);
        ra.addFlashAttribute("success", "Reservation cancelled.");
        return "redirect:/my/books";
    }

    @PostMapping("/{issueId}/return")
    public String returnBook(@PathVariable Long issueId, HttpServletRequest request, RedirectAttributes ra) {
        User member = currentMember();
        if (member == null) {
            return "redirect:/auth/member-login";
        }
        BookIssue issue = circulationService.getIssue(issueId);
        if (issue == null || !issue.getMember().getId().equals(member.getId())) {
            ra.addFlashAttribute("error", "That book is not in your borrowed list.");
            return samePageRedirect(request, "/my/books");
        }
        CirculationService.ReturnResult result = circulationService.returnBook(issueId);
        if (result.success()) {
            ra.addFlashAttribute("success", result.message());
        } else {
            ra.addFlashAttribute("error", result.message());
        }
        return samePageRedirect(request, "/my/books");
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

    private User currentMember() {
        User u = SecurityUtil.currentUser();
        return u != null && u.getRole() == Role.MEMBER ? u : null;
    }
}
