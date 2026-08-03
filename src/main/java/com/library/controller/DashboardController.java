package com.library.controller;

import com.library.model.BookIssue;
import com.library.model.Role;
import com.library.model.User;
import com.library.security.SecurityUtil;
import com.library.service.CirculationService;
import com.library.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final ReportService reportService;
    private final CirculationService circulationService;

    public DashboardController(ReportService reportService, CirculationService circulationService) {
        this.reportService = reportService;
        this.circulationService = circulationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = SecurityUtil.currentUser();
        if (currentUser != null && currentUser.getRole() == Role.MEMBER) {
            List<BookIssue> issues = circulationService.activeIssuesForMember(currentUser);
            model.addAttribute("memberIssues", issues);
            model.addAttribute("memberOverdueCount",
                    issues.stream().filter(BookIssue::isOverdue).count());
            model.addAttribute("memberFines", circulationService.memberFines(currentUser));
            model.addAttribute("memberPendingFineTotal", circulationService.memberPendingFineTotal(currentUser));
            model.addAttribute("memberReservations", circulationService.memberReservations(currentUser));
            return "dashboard-member";
        }
        model.addAllAttributes(reportService.dashboard());
        return "dashboard";
    }
}
