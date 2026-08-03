package com.library.controller;

import com.library.service.CirculationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reservations")
public class ReservationsController {

    private final CirculationService circulationService;

    public ReservationsController(CirculationService circulationService) {
        this.circulationService = circulationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reservations", circulationService.allReservations());
        return "reservations/list";
    }
}
