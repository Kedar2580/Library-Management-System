package com.library.controller;

import com.library.model.User;
import com.library.security.SecurityUtil;
import com.library.service.EngagementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final EngagementService engagementService;

    public ReviewController(EngagementService engagementService) {
        this.engagementService = engagementService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long bookId, Model model) {
        if (bookId != null) {
            model.addAttribute("reviews", engagementService.reviewsForBook(bookId));
            model.addAttribute("bookId", bookId);
        } else {
            model.addAttribute("reviews", engagementService.allReviews());
        }
        return "reviews/list";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long bookId,
                      @RequestParam int rating,
                      @RequestParam(required = false) String comment,
                      RedirectAttributes ra) {
        User user = SecurityUtil.currentUser();
        engagementService.addReview(bookId, user, Math.max(1, Math.min(5, rating)), comment);
        ra.addFlashAttribute("success", "Review submitted.");
        return "redirect:/books/" + bookId;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        engagementService.deleteReview(id);
        ra.addFlashAttribute("success", "Review deleted.");
        return "redirect:/reviews";
    }
}
