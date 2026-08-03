package com.library.controller;

import com.library.model.User;
import com.library.security.SecurityUtil;
import com.library.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String list(Model model) {
        User user = SecurityUtil.currentUser();
        model.addAttribute("notifications", notificationService.forUser(user));
        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String readAll() {
        User user = SecurityUtil.currentUser();
        notificationService.markAllRead(user);
        return "redirect:/notifications";
    }
}
