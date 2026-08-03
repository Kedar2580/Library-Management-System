package com.library.controller;

import com.library.model.Publisher;
import com.library.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/publishers")
public class PublisherController {

    private final BookService bookService;

    public PublisherController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("publishers", bookService.allPublishers());
        return "publishers/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("publisher", new Publisher());
        return "publishers/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("publisher", bookService.getPublisher(id));
        return "publishers/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Publisher publisher, RedirectAttributes ra) {
        if (publisher.getName() == null || publisher.getName().isBlank()) {
            ra.addFlashAttribute("error", "Publisher name is required.");
            return "redirect:/publishers";
        }
        bookService.savePublisher(publisher);
        ra.addFlashAttribute("success", "Publisher saved.");
        return "redirect:/publishers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        bookService.deletePublisher(id);
        ra.addFlashAttribute("success", "Publisher deleted.");
        return "redirect:/publishers";
    }
}
