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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Base64;

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

    @PostMapping("/photo")
    public String uploadPhoto(@RequestParam("photo") MultipartFile photo,
                              RedirectAttributes ra) {
        if (photo.isEmpty()) {
            ra.addFlashAttribute("error", "Please choose an image file.");
            return "redirect:/profile";
        }
        String contentType = photo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            ra.addFlashAttribute("error", "Only image files are allowed.");
            return "redirect:/profile";
        }
        if (photo.getSize() > 5 * 1024 * 1024) {
            ra.addFlashAttribute("error", "Image must be 5 MB or smaller.");
            return "redirect:/profile";
        }
        try {
            String dataUri = "data:" + contentType + ";base64,"
                    + Base64.getEncoder().encodeToString(photo.getBytes());
            userService.updateProfilePhoto(SecurityUtil.currentUser().getId(), dataUri);
            ra.addFlashAttribute("success", "Profile photo updated.");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Could not read the uploaded image.");
        }
        return "redirect:/profile";
    }
}
