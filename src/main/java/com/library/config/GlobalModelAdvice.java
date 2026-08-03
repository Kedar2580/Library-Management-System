package com.library.config;

import com.library.model.Role;
import com.library.model.User;
import com.library.security.SecurityUtil;
import com.library.service.EngagementService;
import com.library.service.NotificationService;
import com.library.service.SettingsService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final NotificationService notificationService;
    private final SettingsService settingsService;
    private final EngagementService engagementService;

    public GlobalModelAdvice(NotificationService notificationService,
                             SettingsService settingsService,
                             EngagementService engagementService) {
        this.notificationService = notificationService;
        this.settingsService = settingsService;
        this.engagementService = engagementService;
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        return SecurityUtil.currentUser();
    }

    @ModelAttribute("unreadNotifications")
    public long unreadNotifications() {
        User user = SecurityUtil.currentUser();
        return user == null ? 0 : notificationService.unreadCount(user);
    }

    @ModelAttribute("libraryName")
    public String libraryName() {
        return settingsService.get("libraryName", "Library");
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        User user = SecurityUtil.currentUser();
        return user != null && user.getRole() == Role.ADMIN;
    }

    @ModelAttribute("isLibrarian")
    public boolean isLibrarian() {
        User user = SecurityUtil.currentUser();
        return user != null && (user.getRole() == Role.LIBRARIAN || user.getRole() == Role.ADMIN);
    }

    @ModelAttribute("isMember")
    public boolean isMember() {
        User user = SecurityUtil.currentUser();
        return user != null && user.getRole() == Role.MEMBER;
    }
}
