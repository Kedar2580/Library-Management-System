package com.library.controller;

import com.library.model.MessageType;
import com.library.service.EngagementService;
import com.library.service.SettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final EngagementService engagementService;

    public FeedbackController(EngagementService engagementService) {
        this.engagementService = engagementService;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("types", MessageType.values());
        return "feedback";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam String name,
                         @RequestParam String email,
                         @RequestParam String subject,
                         @RequestParam String content,
                         @RequestParam MessageType type,
                         RedirectAttributes ra) {
        engagementService.submitMessage(name, email, subject, content, type);
        ra.addFlashAttribute("success", "Thank you! Your message has been received.");
        return "redirect:/feedback";
    }
}
