package com.library.controller;

import com.library.service.PdfService;
import com.library.service.ReportService;
import com.library.repository.BookRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final PdfService pdfService;
    private final BookRepository bookRepository;

    public ReportController(ReportService reportService, PdfService pdfService,
                            BookRepository bookRepository) {
        this.reportService = reportService;
        this.pdfService = pdfService;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public String index() {
        return "reports/index";
    }

    @GetMapping("/view")
    public String view(@RequestParam String type,
                       @RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       @RequestParam(required = false) String day,
                       @RequestParam(required = false) Integer year,
                       @RequestParam(required = false) Integer month,
                       Model model) {
        ReportService.Report report = buildReport(type, from, to, day, year, month);
        model.addAttribute("report", report);
        model.addAttribute("type", type);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("day", day);
        model.addAttribute("year", year == null ? LocalDate.now().getYear() : year);
        model.addAttribute("month", month == null ? LocalDate.now().getMonthValue() : month);
        return "reports/view";
    }

    @GetMapping("/export")
    public ResponseEntity<String> export(@RequestParam String type,
                                         @RequestParam(required = false) String from,
                                         @RequestParam(required = false) String to,
                                         @RequestParam(required = false) String day,
                                         @RequestParam(required = false) Integer year,
                                         @RequestParam(required = false) Integer month) {
        ReportService.Report report = buildReport(type, from, to, day, year, month);
        String csv = String.join(",", report.headers) + "\n"
                + report.rows.stream()
                .map(r -> java.util.Arrays.stream(r).map(cell -> "\"" + cell.replace("\"", "\"\"") + "\"")
                        .collect(Collectors.joining(",")))
                .collect(Collectors.joining("\n"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + "-report.csv")
                .body(csv);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam String type,
                                            @RequestParam(required = false) String from,
                                            @RequestParam(required = false) String to,
                                            @RequestParam(required = false) String day,
                                            @RequestParam(required = false) Integer year,
                                            @RequestParam(required = false) Integer month) {
        ReportService.Report report = buildReport(type, from, to, day, year, month);
        byte[] pdf = pdfService.reportPdf(report);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + "-report.pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/catalog/pdf")
    public ResponseEntity<byte[]> catalogPdf() {
        byte[] pdf = pdfService.catalogPdf(bookRepository.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=library-catalog.pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private ReportService.Report buildReport(String type, String from, String to,
                                             String day, Integer year, Integer month) {
        LocalDate defaultFrom = LocalDate.now().minusDays(30);
        LocalDate defaultTo = LocalDate.now();
        return switch (type == null ? "" : type) {
            case "issued" -> reportService.issuedBooksReport(
                    parse(from, defaultFrom), parse(to, defaultTo));
            case "returned" -> reportService.returnedBooksReport(
                    parse(from, defaultFrom), parse(to, defaultTo));
            case "fines" -> reportService.fineReport(
                    parse(from, defaultFrom), parse(to, defaultTo));
            case "inventory" -> reportService.inventoryReport();
            case "daily" -> reportService.dailyReport(parse(day, LocalDate.now()));
            case "monthly" -> reportService.monthlyReport(
                    year == null ? LocalDate.now().getYear() : year,
                    month == null ? LocalDate.now().getMonthValue() : month);
            default -> reportService.issuedBooksReport(defaultFrom, defaultTo);
        };
    }

    private LocalDate parse(String value, LocalDate fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return fallback;
        }
    }
}
