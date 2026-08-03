package com.library.controller;

import com.library.model.Role;
import com.library.model.User;
import com.library.security.CustomUserDetails;
import com.library.security.SecurityUtil;
import com.library.service.BackupService;
import com.library.service.EngagementService;
import com.library.service.SettingsService;
import com.library.service.UserService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final SettingsService settingsService;
    private final BackupService backupService;
    private final EngagementService engagementService;
    private final SessionRegistry sessionRegistry;

    public AdminController(UserService userService, SettingsService settingsService,
                           BackupService backupService, EngagementService engagementService,
                           SessionRegistry sessionRegistry) {
        this.userService = userService;
        this.settingsService = settingsService;
        this.backupService = backupService;
        this.engagementService = engagementService;
        this.sessionRegistry = sessionRegistry;
    }

    @GetMapping
    public String panel() {
        return "redirect:/admin/users";
    }

    // ----- Active sessions -----

    @GetMapping("/sessions")
    public String sessions(Model model) {
        List<ActiveSession> sessions = new ArrayList<>();
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (!(principal instanceof CustomUserDetails details)) {
                continue;
            }
            User user = details.getUser();
            for (SessionInformation info : sessionRegistry.getAllSessions(principal, false)) {
                sessions.add(new ActiveSession(
                        user.getFullName(),
                        user.getUsername(),
                        user.getRole().getLabel(),
                        info.getSessionId(),
                        info.getLastRequest()));
            }
        }
        sessions.sort(Comparator.comparing(ActiveSession::lastRequest).reversed());
        model.addAttribute("activeSessions", sessions);
        return "admin/sessions";
    }

    public record ActiveSession(String fullName, String username, String role,
                                String sessionId, Date lastRequest) {
    }

    // ----- User & role management -----

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.allUsers());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("roles", Role.values());
        return "admin/user-form";
    }

    @PostMapping("/users/save")
    public String saveUser(@RequestParam(required = false) Long id,
                           @RequestParam String username,
                           @RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam Role role,
                           @RequestParam(required = false) String password,
                           @RequestParam(defaultValue = "true") boolean enabled,
                           RedirectAttributes ra) {
        if (id != null) {
            User user = userService.get(id);
            if (user != null) {
                user.setUsername(username);
                user.setFullName(fullName);
                user.setEmail(email);
                user.setPhone(phone);
                user.setRole(role);
                user.setEnabled(enabled);
                if (password != null && !password.isBlank()) {
                    userService.changePassword(user, password);
                }
                userService.updateUser(user);
                ra.addFlashAttribute("success", "User updated.");
            }
        } else {
            if (userService.isUsernameTaken(username)) {
                ra.addFlashAttribute("error", "Username already exists.");
                return "redirect:/admin/users";
            }
            userService.createUser(username, email, fullName, phone, role, password);
            ra.addFlashAttribute("success", "User created.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        User user = userService.get(id);
        if (user != null && user.getRole() != Role.ADMIN) {
            userService.toggleEnabled(id);
            ra.addFlashAttribute("success", "User enabled status toggled.");
        }
        return "redirect:/admin/users";
    }

    // ----- Staff account management -----

    @GetMapping("/staff")
    public String staff(Model model) {
        model.addAttribute("staff", userService.staffMembers());
        return "admin/staff";
    }

    @GetMapping("/staff/new")
    public String newStaff(Model model) {
        model.addAttribute("staffRoles", List.of(Role.ADMIN, Role.LIBRARIAN));
        return "admin/staff-form";
    }

    @PostMapping("/staff/save")
    public String saveStaff(@RequestParam String username,
                            @RequestParam String fullName,
                            @RequestParam String email,
                            @RequestParam String phone,
                            @RequestParam Role role,
                            @RequestParam String password,
                            RedirectAttributes ra) {
        if (role != Role.ADMIN && role != Role.LIBRARIAN) {
            ra.addFlashAttribute("error", "Staff accounts can only be Administrator or Librarian.");
            return "redirect:/admin/staff";
        }
        if (userService.isUsernameTaken(username)) {
            ra.addFlashAttribute("error", "Username already exists.");
            return "redirect:/admin/staff";
        }
        if (userService.isEmailTaken(email)) {
            ra.addFlashAttribute("error", "Email already registered.");
            return "redirect:/admin/staff";
        }
        userService.createUser(username, email, fullName, phone, role, password);
        ra.addFlashAttribute("success", "Staff account '" + username + "' created.");
        return "redirect:/admin/staff";
    }

    // ----- Feedback messages -----

    @GetMapping("/feedback")
    public String feedback(Model model) {
        model.addAttribute("messages", engagementService.allMessages());
        return "admin/feedback";
    }

    @PostMapping("/feedback/{id}/status")
    public String feedbackStatus(@PathVariable Long id,
                                 @RequestParam String status,
                                 RedirectAttributes ra) {
        engagementService.setMessageStatus(id, com.library.model.MessageStatus.valueOf(status));
        ra.addFlashAttribute("success", "Message status updated.");
        return "redirect:/admin/feedback";
    }

    // ----- System settings -----

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("settings", settingsService.all());
        return "admin/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@RequestParam Map<String, String> allParams, RedirectAttributes ra) {
        allParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("setting_"))
                .forEach(e -> settingsService.set(
                        e.getKey().substring("setting_".length()), e.getValue()));
        ra.addFlashAttribute("success", "Settings saved.");
        return "redirect:/admin/settings";
    }

    // ----- Backup & restore -----

    @GetMapping("/backup")
    public String backupPage() {
        return "admin/backup";
    }

    @GetMapping("/backup/download")
    public ResponseEntity<FileSystemResource> downloadBackup() throws Exception {
        Path file = backupService.createBackup();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + file.getFileName())
                .contentType(MediaType.parseMediaType("application/sql"))
                .body(new FileSystemResource(file));
    }

    @PostMapping("/restore")
    public String restore(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "No file selected.");
            return "redirect:/admin/backup";
        }
        try {
            Path tmp = Files.createTempFile("restore", ".sql");
            file.transferTo(tmp);
            backupService.restore(tmp);
            ra.addFlashAttribute("success", "Database restored. Please log in again.");
            return "redirect:/auth/logout";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Restore failed: " + e.getMessage());
            return "redirect:/admin/backup";
        }
    }
}
