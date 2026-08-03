package com.library.controller;

import com.library.model.Category;
import com.library.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final BookService bookService;

    public CategoryController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", bookService.allCategories());
        return "categories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", bookService.getCategory(id));
        return "categories/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Category category, RedirectAttributes ra) {
        if (category.getName() == null || category.getName().isBlank()) {
            ra.addFlashAttribute("error", "Category name is required.");
            return "redirect:/categories";
        }
        bookService.saveCategory(category);
        ra.addFlashAttribute("success", "Category saved.");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        bookService.deleteCategory(id);
        ra.addFlashAttribute("success", "Category deleted.");
        return "redirect:/categories";
    }
}
