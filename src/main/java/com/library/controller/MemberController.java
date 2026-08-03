package com.library.controller;

import com.library.model.MembershipStatus;
import com.library.model.User;
import com.library.service.CirculationService;
import com.library.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final UserService userService;
    private final CirculationService circulationService;

    public MemberController(UserService userService, CirculationService circulationService) {
        this.userService = userService;
        this.circulationService = circulationService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("members", userService.searchMembers(q));
        model.addAttribute("q", q == null ? "" : q);
        return "members/list";
    }

    @GetMapping("/{id}/profile")
    public String profile(@PathVariable Long id, Model model) {
        User member = userService.get(id);
        if (member == null) {
            return "redirect:/members";
        }
        model.addAttribute("member", member);
        model.addAttribute("issues", circulationService.memberIssues(member));
        model.addAttribute("fines", circulationService.memberFines(member));
        model.addAttribute("reservations", circulationService.memberReservations(member));
        return "members/profile";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam MembershipStatus status,
                               RedirectAttributes ra) {
        User member = userService.get(id);
        if (member != null) {
            member.setMembershipStatus(status);
            userService.updateUser(member);
            ra.addFlashAttribute("success", "Membership status updated to " + status.getLabel() + ".");
        }
        return "redirect:/members/" + id + "/profile";
    }
}
