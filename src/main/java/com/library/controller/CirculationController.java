package com.library.controller;

import com.library.model.Book;
import com.library.model.Role;
import com.library.model.User;
import com.library.security.SecurityUtil;
import com.library.service.BookService;
import com.library.service.CirculationService;
import com.library.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/circulation")
public class CirculationController {

    private final CirculationService circulationService;
    private final BookService bookService;
    private final UserService userService;

    public CirculationController(CirculationService circulationService,
                                 BookService bookService, UserService userService) {
        this.circulationService = circulationService;
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping("/issue")
    public String issueForm(@RequestParam(required = false) Long bookId,
                            @RequestParam(required = false) Long memberId,
                            Model model) {
        List<Book> available = bookService.allBooks().stream()
                .filter(Book::isAvailable)
                .toList();
        model.addAttribute("availableBooks", available);
        model.addAttribute("members", userService.allMembers());
        model.addAttribute("selectedBook", bookId);
        model.addAttribute("selectedMember", memberId != null
                ? memberId : currentMemberId());
        return "circulation/issue";
    }

    @PostMapping("/issue")
    public String issue(@RequestParam Long bookId,
                        @RequestParam Long memberId,
                        RedirectAttributes ra) {
        String error = circulationService.issueBook(bookId, memberId);
        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/circulation/issue?bookId=" + bookId + "&memberId=" + memberId;
        }
        ra.addFlashAttribute("success", "Book issued successfully. Thank you!");
        return postActionRedirect("/issues");
    }

    @GetMapping("/return")
    public String returnForm(Model model) {
        model.addAttribute("activeIssues", circulationService.activeIssues());
        return "circulation/return";
    }

    @PostMapping("/return")
    public String returnBook(@RequestParam Long issueId, RedirectAttributes ra) {
        var result = circulationService.returnBook(issueId);
        if (result.success()) {
            ra.addFlashAttribute("success", result.message());
        } else {
            ra.addFlashAttribute("error", result.message());
        }
        return postActionRedirect("/returns");
    }

    @GetMapping("/reserve")
    public String reserveForm(Model model) {
        List<Book> unavailable = bookService.allBooks().stream()
                .filter(b -> !b.isAvailable())
                .toList();
        model.addAttribute("unavailableBooks", unavailable);
        model.addAttribute("members", userService.allMembers());
        return "circulation/reserve";
    }

    @PostMapping("/reserve")
    public String reserve(@RequestParam Long bookId,
                          @RequestParam Long memberId,
                          RedirectAttributes ra) {
        String error = circulationService.reserveBook(bookId, memberId);
        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/circulation/reserve";
        }
        ra.addFlashAttribute("success", "Book reserved. You will be notified when it is available.");
        return postActionRedirect("/reservations");
    }

    @PostMapping("/reservation/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, RedirectAttributes ra) {
        circulationService.cancelReservation(id);
        ra.addFlashAttribute("success", "Reservation cancelled.");
        return postActionRedirect("/reservations");
    }

    @GetMapping("/issue/select/{bookId}")
    public String selectBook(@PathVariable Long bookId, Model model) {
        List<Book> available = bookService.allBooks().stream()
                .filter(Book::isAvailable)
                .toList();
        model.addAttribute("availableBooks", available);
        model.addAttribute("members", userService.allMembers());
        model.addAttribute("selectedBook", bookId);
        model.addAttribute("selectedMember", currentMemberId());
        return "circulation/issue";
    }

    private Long currentMemberId() {
        User u = SecurityUtil.currentUser();
        return u != null && u.getRole() == Role.MEMBER ? u.getId() : null;
    }

    private String postActionRedirect(String staffTarget) {
        User u = SecurityUtil.currentUser();
        boolean staff = u != null && (u.getRole() == Role.ADMIN || u.getRole() == Role.LIBRARIAN);
        return staff ? "redirect:" + staffTarget : "redirect:/dashboard";
    }
}
