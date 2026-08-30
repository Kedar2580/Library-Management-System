package com.library.controller;

import com.library.model.Fine;
import com.library.model.FineStatus;
import com.library.model.PaymentMethod;
import com.library.model.Role;
import com.library.model.User;
import com.library.security.SecurityUtil;
import com.library.service.CirculationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/my/fines")
public class MyFinesController {

    private final CirculationService circulationService;

    public MyFinesController(CirculationService circulationService) {
        this.circulationService = circulationService;
    }

    @GetMapping
    public String fines(Model model) {
        User member = currentMember();
        if (member == null) {
            return "redirect:/auth/member-login";
        }
        model.addAttribute("memberFines", circulationService.memberFines(member));
        model.addAttribute("pendingTotal", circulationService.memberPendingFineTotal(member));
        return "my/fines";
    }

    @PostMapping("/pay")
    public String pay(@RequestParam(required = false) List<Long> fineIds,
                      @RequestParam(required = false) String paymentMethod,
                      @RequestParam(required = false) String reference,
                      RedirectAttributes ra) {
        User member = currentMember();
        if (member == null) {
            return "redirect:/auth/member-login";
        }
        if (fineIds == null || fineIds.isEmpty()) {
            ra.addFlashAttribute("error", "Select at least one fine to pay.");
            return "redirect:/my/fines";
        }
        List<Long> ownPending = circulationService.memberFines(member).stream()
                .filter(f -> f.getStatus() == FineStatus.PENDING)
                .map(Fine::getId)
                .filter(fineIds::contains)
                .toList();
        if (ownPending.isEmpty()) {
            ra.addFlashAttribute("error", "No payable fines selected for your account.");
            return "redirect:/my/fines";
        }
        PaymentMethod method = null;
        try {
            method = PaymentMethod.valueOf(paymentMethod);
        } catch (Exception ignored) {
        }
        int paid = circulationService.payFines(ownPending, method, reference);
        ra.addFlashAttribute("success", paid + " fine(s) paid successfully. Thank you!");
        return "redirect:/my/fines";
    }

    private User currentMember() {
        User u = SecurityUtil.currentUser();
        return u != null && u.getRole() == Role.MEMBER ? u : null;
    }
}
