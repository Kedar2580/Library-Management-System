package com.library.controller;

import com.library.model.Role;
import com.library.model.User;
import com.library.security.SecurityUtil;
import com.library.service.SettingsService;
import com.library.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final SettingsService settingsService;

    public AuthController(UserService userService, SettingsService settingsService) {
        this.userService = userService;
        this.settingsService = settingsService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        if (SecurityUtil.currentUser() != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("portalAnnouncement",
                settingsService.get("portalAnnouncement",
                        "Welcome to the Library! Please return borrowed books on time to avoid fines.\n"
                                + "Library hours: Mon-Fri 9:00 AM - 7:00 PM, Sat 10:00 AM - 4:00 PM, Sun closed.\n"
                                + "New arrivals are updated every Friday. Maximum 5 books can be borrowed per member."));
        model.addAttribute("contactEmail", settingsService.get("contactEmail", "library@example.com"));
        model.addAttribute("contactPhone", settingsService.get("contactPhone", "+1 (555) 123-4567"));
        model.addAttribute("address", settingsService.get("address", "1 Library Street, Downtown"));
        return "auth/portal";
    }

    @GetMapping("/member-login")
    public String memberLogin(@RequestParam(required = false) String roleError, Model model) {
        if (SecurityUtil.currentUser() != null) {
            return "redirect:/dashboard";
        }
        if ("member".equals(roleError)) {
            model.addAttribute("roleError",
                    "This portal is for user accounts only. Staff should use the staff login.");
        }
        return "auth/member-login";
    }

    @GetMapping("/admin-login")
    public String adminLogin(@RequestParam(required = false) String roleError,
                             @RequestParam(required = false) String register, Model model) {
        if (SecurityUtil.currentUser() != null) {
            return "redirect:/dashboard";
        }
        if ("staff".equals(roleError)) {
            model.addAttribute("roleError",
                    "This portal is for staff accounts (Administrator / Librarian) only. Users should use the user login.");
        }
        if (register != null) {
            model.addAttribute("staffFormOpen", true);
        }
        return "auth/admin-login";
    }

    @PostMapping("/admin-register")
    public String registerStaff(@RequestParam String username,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam Role role,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                RedirectAttributes ra) {
        if (role != Role.ADMIN && role != Role.LIBRARIAN) {
            ra.addFlashAttribute("error", "Staff accounts can only be Administrator or Librarian.");
            return "redirect:/auth/admin-login?register=1";
        }
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/auth/admin-login?register=1";
        }
        if (userService.isUsernameTaken(username)) {
            ra.addFlashAttribute("error", "Username is already taken.");
            return "redirect:/auth/admin-login?register=1";
        }
        if (userService.isEmailTaken(email)) {
            ra.addFlashAttribute("error", "Email is already registered.");
            return "redirect:/auth/admin-login?register=1";
        }
        userService.createUser(username, email, fullName, phone, role, password);
        ra.addFlashAttribute("success", "Staff account '" + username + "' created. You can now login.");
        return "redirect:/auth/admin-login?register=1";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String fullName,
                           @RequestParam String phone,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           RedirectAttributes ra) {
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/auth/register";
        }
        if (userService.isUsernameTaken(username)) {
            ra.addFlashAttribute("error", "Username is already taken.");
            return "redirect:/auth/register";
        }
        if (userService.isEmailTaken(email)) {
            ra.addFlashAttribute("error", "Email is already registered.");
            return "redirect:/auth/register";
        }
        userService.registerMember(username, email, fullName, phone, password);
        ra.addFlashAttribute("success", "Registration successful. You can now login.");
        return "redirect:/auth/member-login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 RedirectAttributes ra, Model model) {
        String token = userService.createResetToken(email);
        if (token == null) {
            ra.addFlashAttribute("error", "No account found with that email.");
            return "redirect:/auth/forgot-password";
        }
        model.addAttribute("token", token);
        return "auth/check-email";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        if (userService.findByResetToken(token) == null) {
            return "auth/invalid-token";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                RedirectAttributes ra) {
        User user = userService.findByResetToken(token);
        if (user == null) {
            ra.addFlashAttribute("error", "Reset link is invalid or expired.");
            return "redirect:/auth/member-login";
        }
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/auth/reset-password?token=" + token;
        }
        userService.changePassword(user, password);
        ra.addFlashAttribute("success", "Password reset successful. Please login.");
        return "redirect:/auth/member-login";
    }

    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "auth/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {
        User user = SecurityUtil.currentUser();
        if (user == null) {
            return "redirect:/auth/member-login";
        }
        if (!userService.passwordMatches(user, currentPassword)) {
            ra.addFlashAttribute("error", "Current password is incorrect.");
            return "redirect:/auth/change-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/auth/change-password";
        }
        userService.changePassword(user, newPassword);
        ra.addFlashAttribute("success", "Password changed successfully.");
        return "redirect:/dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}
