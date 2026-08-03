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
            books = mostBorrowed(4);
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

    private List<Book> mostBorrowed(int limit) {
        Map<Book, Long> counts = borrowCounts();
        if (!counts.isEmpty()) {
            return counts.entrySet().stream()
                    .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .toList();
        }
        List<Book> shuffled = new ArrayList<>(bookRepository.findAll());
        Collections.shuffle(shuffled);
        return shuffled.stream().limit(limit).toList();
    }
}
