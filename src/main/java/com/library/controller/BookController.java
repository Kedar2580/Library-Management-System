package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import com.library.service.CsvService;
import com.library.service.EngagementService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final CsvService csvService;
    private final EngagementService engagementService;
    private final com.library.service.RatingSyncService ratingSyncService;

    public BookController(BookService bookService, CsvService csvService,
                          EngagementService engagementService,
                          com.library.service.RatingSyncService ratingSyncService) {
        this.bookService = bookService;
        this.csvService = csvService;
        this.engagementService = engagementService;
        this.ratingSyncService = ratingSyncService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("books", bookService.search(q));
        model.addAttribute("q", q == null ? "" : q);
        return "books/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        Book book = bookService.getBook(id);
        if (book == null) {
            return "redirect:/books";
        }
        model.addAttribute("book", book);
        model.addAttribute("reviews", engagementService.reviewsForBook(id));
        return "books/details";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", bookService.allCategories());
        model.addAttribute("authors", bookService.allAuthors());
        model.addAttribute("publishers", bookService.allPublishers());
        return "books/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBook(id);
        if (book == null) {
            return "redirect:/books";
        }
        model.addAttribute("book", book);
        model.addAttribute("categories", bookService.allCategories());
        model.addAttribute("authors", bookService.allAuthors());
        model.addAttribute("publishers", bookService.allPublishers());
        return "books/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Book book,
                       RedirectAttributes ra) {
        if (book.getCategory() != null && book.getCategory().getId() == null) {
            book.setCategory(null);
        }
        if (book.getAuthor() != null && book.getAuthor().getId() == null) {
            book.setAuthor(null);
        }
        if (book.getPublisher() != null && book.getPublisher().getId() == null) {
            book.setPublisher(null);
        }
        if (book.getId() == null) {
            book.setTotalCopies(book.getTotalCopies() == 0 ? 1 : book.getTotalCopies());
            book.setAvailableCopies(book.getTotalCopies());
            book.setReviewCount(0);
            book.setAvgRating(0);
        } else {
            Book existing = bookService.getBook(book.getId());
            int diff = book.getTotalCopies() - existing.getTotalCopies();
            book.setAvailableCopies(existing.getAvailableCopies() + diff);
        }
        bookService.saveBook(book);
        ra.addFlashAttribute("success", "Book saved successfully.");
        return "redirect:/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        bookService.deleteBook(id);
        ra.addFlashAttribute("success", "Book deleted.");
        return "redirect:/books";
    }

    @GetMapping("/import")
    public String importForm() {
        return "books/import";
    }

    @PostMapping("/import")
    public String importBooks(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        try {
            List<String[]> rows = csvService.parse(file);
            int imported = 0;
            for (String[] r : rows) {
                if (r[0].isBlank()) {
                    continue;
                }
                Book b = new Book();
                b.setTitle(r[0]);
                b.setIsbn(r[1].isBlank() ? null : r[1]);
                b.setAuthor(bookService.findOrCreateAuthor(r[2]));
                b.setCategory(bookService.findOrCreateCategory(r[3]));
                b.setPublisher(bookService.findOrCreatePublisher(r[4]));
                try {
                    b.setPublicationYear(r[5].isBlank() ? null : Integer.parseInt(r[5]));
                } catch (NumberFormatException e) {
                    b.setPublicationYear(null);
                }
                int copies = 1;
                try {
                    copies = r[6].isBlank() ? 1 : Integer.parseInt(r[6]);
                } catch (NumberFormatException ignored) {
                }
                b.setTotalCopies(copies);
                b.setAvailableCopies(copies);
                b.setShelfLocation(r[7]);
                bookService.saveBook(b);
                imported++;
            }
            ra.addFlashAttribute("success", "Imported " + imported + " books.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }
        return "redirect:/books";
    }

    @PostMapping("/ratings/sync")
    public String syncRatings(RedirectAttributes ra) {
        try {
            String result = ratingSyncService.syncAll();
            ra.addFlashAttribute("success", result);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Rating sync failed: " + e.getMessage());
        }
        return "redirect:/books";
    }

    @GetMapping("/export")
    public ResponseEntity<String> export() {
        String csv = csvService.exportBooks(bookService.allBooks());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }
}
