package com.library.controller;

import com.library.model.User;
import com.library.security.SecurityUtil;
import com.library.service.CirculationService;
import com.library.service.EngagementService;
import com.library.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final CirculationService circulationService;
    private final EngagementService engagementService;

    public ProfileController(UserService userService, CirculationService circulationService,
                             EngagementService engagementService) {
        this.userService = userService;
        this.circulationService = circulationService;
        this.engagementService = engagementService;
    }

    @GetMapping
    public String profile(Model model) {
        User user = SecurityUtil.currentUser();
        model.addAttribute("user", user);
        model.addAttribute("issues", circulationService.memberIssues(user));
        model.addAttribute("fines", circulationService.memberFines(user));
        model.addAttribute("reservations", circulationService.memberReservations(user));
        model.addAttribute("reviews", engagementService.reviewsByUser(user));
        return "profile";
    }

    @PostMapping("/edit")
    public String edit(@RequestParam String fullName,
                       @RequestParam String email,
                       @RequestParam String phone,
                       @RequestParam String address,
                       RedirectAttributes ra) {
        User user = SecurityUtil.currentUser();
        userService.updateProfile(user.getId(), fullName, phone, email, address);
        ra.addFlashAttribute("success", "Profile updated.");
        return "redirect:/profile";
    }
}
