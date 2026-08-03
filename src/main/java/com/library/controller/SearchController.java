package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final BookService bookService;

    public SearchController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String search(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String by,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Boolean availableOnly,
                         Model model) {
        String query = q == null ? "" : q;
        List<Book> results;

        if ("title".equals(by)) {
            results = bookService.searchByTitleOrIsbn(query);
        } else if ("author".equals(by)) {
            results = bookService.searchByAuthor(query);
        } else if ("category".equals(by)) {
            results = bookService.searchByCategory(query);
        } else if ("isbn".equals(by)) {
            results = bookService.searchByTitleOrIsbn(query);
        } else {
            results = bookService.search(query);
        }

        if (categoryId != null) {
            results = results.stream()
                    .filter(b -> b.getCategory() != null && b.getCategory().getId().equals(categoryId))
                    .toList();
        }
        if (Boolean.TRUE.equals(availableOnly)) {
            results = results.stream().filter(Book::isAvailable).toList();
        }

        model.addAttribute("results", results);
        model.addAttribute("q", query);
        model.addAttribute("by", by == null ? "all" : by);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("availableOnly", availableOnly);
        model.addAttribute("categories", bookService.allCategories());
        return "search";
    }
}
