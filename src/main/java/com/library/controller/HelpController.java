package com.library.controller;

import com.library.service.SettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/help")
public class HelpController {

    private final SettingsService settingsService;

    public HelpController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public String help(Model model) {
        model.addAttribute("contactEmail", settingsService.get("contactEmail", "library@example.com"));
        model.addAttribute("contactPhone", settingsService.get("contactPhone", ""));
        model.addAttribute("address", settingsService.get("address", ""));
        return "help";
    }
}
