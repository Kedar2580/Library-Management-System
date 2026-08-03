package com.library.controller;

import com.library.model.Author;
import com.library.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/authors")
public class AuthorController {

    private final BookService bookService;

    public AuthorController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("authors", bookService.allAuthors());
        return "authors/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("author", new Author());
        return "authors/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("author", bookService.getAuthor(id));
        return "authors/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Author author, RedirectAttributes ra) {
        if (author.getName() == null || author.getName().isBlank()) {
            ra.addFlashAttribute("error", "Author name is required.");
            return "redirect:/authors";
        }
        bookService.saveAuthor(author);
        ra.addFlashAttribute("success", "Author saved.");
        return "redirect:/authors";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        bookService.deleteAuthor(id);
        ra.addFlashAttribute("success", "Author deleted.");
        return "redirect:/authors";
    }
}
