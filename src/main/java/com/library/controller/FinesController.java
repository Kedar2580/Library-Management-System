package com.library.controller;

import com.library.model.PaymentMethod;
import com.library.service.CirculationService;
import com.library.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/fines")
public class FinesController {

    private final CirculationService circulationService;
    private final UserService userService;

    public FinesController(CirculationService circulationService, UserService userService) {
        this.circulationService = circulationService;
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pendingFines", circulationService.pendingFines());
        model.addAttribute("paidFines", circulationService.paidFines());
        model.addAttribute("pendingTotal", circulationService.pendingFineTotal());
        model.addAttribute("paidTotal", circulationService.paidFineTotal());
        return "fines/list";
    }

    @GetMapping("/new")
    public String newFine(Model model) {
        model.addAttribute("members", userService.allMembers());
        return "fines/form";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long memberId,
                      @RequestParam double amount,
                      @RequestParam(required = false) String reason,
                      RedirectAttributes ra) {
        String error = circulationService.createManualFine(memberId, amount, reason);
        if (error != null) {
            ra.addFlashAttribute("error", error);
            return "redirect:/fines/new";
        }
        ra.addFlashAttribute("success", "Fine added successfully.");
        return "redirect:/fines";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id, RedirectAttributes ra) {
        circulationService.payFine(id);
        ra.addFlashAttribute("success", "Fine marked as paid.");
        return "redirect:/fines";
    }

    @GetMapping("/pay")
    public String paymentForm(@RequestParam(required = false) Long fineId, Model model) {
        model.addAttribute("pendingFines", circulationService.pendingFines());
        model.addAttribute("preselectId", fineId);
        return "fines/pay";
    }

    @PostMapping("/pay")
    public String processPayment(@RequestParam(required = false) List<Long> fineIds,
                                 @RequestParam(required = false) String paymentMethod,
                                 @RequestParam(required = false) String reference,
                                 RedirectAttributes ra) {
        if (fineIds == null || fineIds.isEmpty()) {
            ra.addFlashAttribute("error", "Select at least one fine to pay.");
            return "redirect:/fines/pay";
        }
        PaymentMethod method = null;
        try {
            method = PaymentMethod.valueOf(paymentMethod);
        } catch (Exception ignored) {
        }
        int paid = circulationService.payFines(fineIds, method, reference);
        ra.addFlashAttribute("success", paid + " fine(s) marked as paid.");
        return "redirect:/fines";
    }
}
