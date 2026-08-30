package com.library.controller;

import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.Role;
import com.library.model.User;
import com.library.repository.BookIssueRepository;
import com.library.repository.BookRepository;
import com.library.security.SecurityUtil;
import com.library.service.BookService;
import com.library.service.CirculationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final BookIssueRepository issueRepository;
    private final CirculationService circulationService;

    public HomeController(BookService bookService, BookRepository bookRepository,
                          BookIssueRepository issueRepository,
                          CirculationService circulationService) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.issueRepository = issueRepository;
        this.circulationService = circulationService;
    }

    private static final List<String> WORLD_BESTSELLERS = List.of(
            "Don Quixote", "A Tale of Two Cities", "The Little Prince", "The Alchemist",
            "Harry Potter and the Sorcerer's Stone", "The Hobbit", "The Da Vinci Code",
            "The Catcher in the Rye", "One Hundred Years of Solitude", "The Great Gatsby",
            "Think and Grow Rich", "How to Win Friends and Influence People",
            "The 7 Habits of Highly Effective People", "Atomic Habits", "The Secret",
            "Mindset: The New Psychology of Success");

    @GetMapping({"/", "/home"})
    public String home(@RequestParam(required = false) String q, Model model) {
        User user = SecurityUtil.currentUser();
        if (user != null && user.getRole() == Role.MEMBER) {
            return memberHome(model, user);
        }
        List<Book> books;
        if (StringUtils.hasText(q)) {
            books = bookService.search(q.trim());
        } else {
            books = topSelling(4);
        }
        model.addAttribute("books", books);
        model.addAttribute("q", q);
        return "home";
    }

    private String memberHome(Model model, User member) {
        List<Book> available = new ArrayList<>(bookService.allBooks().stream()
                .filter(Book::isAvailable)
                .toList());
        Map<Book, Long> counts = borrowCounts();
        available.sort(Comparator.comparingLong((Book b) -> counts.getOrDefault(b, 0L)).reversed());
        model.addAttribute("books", available);
        model.addAttribute("myIssues", circulationService.activeIssuesForMember(member));
        return "member-home";
    }

    private Map<Book, Long> borrowCounts() {
        Map<Book, Long> counts = new HashMap<>();
        for (BookIssue issue : issueRepository.findAll()) {
            counts.merge(issue.getBook(), 1L, Long::sum);
        }
        return counts;
    }

    /**
     * Returns the world's top-selling books from the catalog, shuffled into a
     * different order on every visit so the home page always feels fresh.
     */
    private List<Book> topSelling(int limit) {
        List<Book> bestsellers = new ArrayList<>(bookRepository.findAll().stream()
                .filter(b -> matchesBestseller(b.getTitle()))
                .toList());
        if (bestsellers.isEmpty()) {
            bestsellers = new ArrayList<>(bookRepository.findAll());
        }
        Collections.shuffle(bestsellers);
        return bestsellers.stream().limit(limit).toList();
    }

    private boolean matchesBestseller(String title) {
        if (title == null || title.isBlank()) {
            return false;
        }
        String t = title.toLowerCase();
        return WORLD_BESTSELLERS.stream()
                .anyMatch(b -> t.contains(b.toLowerCase()) || b.toLowerCase().contains(t));
    }
}
