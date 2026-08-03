package com.library.controller;

import com.library.service.CirculationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/returns")
public class ReturnsController {

    private final CirculationService circulationService;

    public ReturnsController(CirculationService circulationService) {
        this.circulationService = circulationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeIssues", circulationService.activeIssues());
        model.addAttribute("returnedIssues", circulationService.returnedIssues());
        return "returns/list";
    }
}
