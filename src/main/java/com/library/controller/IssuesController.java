package com.library.controller;

import com.library.service.CirculationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/issues")
public class IssuesController {

    private final CirculationService circulationService;

    public IssuesController(CirculationService circulationService) {
        this.circulationService = circulationService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "all") String view, Model model) {
        model.addAttribute("view", view);
        model.addAttribute("activeIssues", circulationService.activeIssues());
        model.addAttribute("allIssues", circulationService.allIssues());
        model.addAttribute("returnedIssues", circulationService.returnedIssues());
        return "issues/list";
    }
}
