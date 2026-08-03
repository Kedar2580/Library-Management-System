package com.library.service;

import com.library.model.*;
import com.library.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    private final BookRepository bookRepository;
    private final BookIssueRepository issueRepository;
    private final FineRepository fineRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ActivityRepository activityRepository;
    private final CirculationService circulationService;

    public ReportService(BookRepository bookRepository, BookIssueRepository issueRepository,
                         FineRepository fineRepository, UserRepository userRepository,
                         ReservationRepository reservationRepository, ActivityRepository activityRepository,
                         CirculationService circulationService) {
        this.bookRepository = bookRepository;
        this.issueRepository = issueRepository;
        this.fineRepository = fineRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.activityRepository = activityRepository;
        this.circulationService = circulationService;
    }

    // ----- Dashboard -----

    public Map<String, Object> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalBooks", bookRepository.count());
        data.put("availableBooks", bookRepository.countByAvailableCopiesGreaterThan(0));
        data.put("issuedBooks", circulationService.activeCount());
        data.put("totalMembers", userRepository.countByRole(Role.MEMBER));
        data.put("overdueBooks", circulationService.overdueCount());
        data.put("fineCollection", circulationService.paidFineTotal());
        data.put("pendingFineCollection", circulationService.pendingFineTotal());
        data.put("pendingReservations", reservationRepository.countByStatus(ReservationStatus.PENDING));
        data.put("activities", activityRepository.findTop20ByOrderByCreatedAtDesc());
        data.put("overdueIssues", circulationService.activeIssues().stream()
                .filter(BookIssue::isOverdue)
                .limit(10)
                .toList());

        Map<String, Long> last7Days = new LinkedHashMap<>();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("MMM dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            last7Days.put(day.format(f),
                    (long) issueRepository.findByIssueDateBetween(day, day).size());
        }
        data.put("last7Days", last7Days);
        data.put("chartLabels", last7Days.keySet().stream().toList());
        data.put("chartValues", last7Days.values().stream().toList());
        data.put("topBooks", topBorrowedBooks(5));
        return data;
    }

    public List<Object[]> topBorrowedBooks(int limit) {
        Map<Book, Long> counts = new HashMap<>();
        for (BookIssue issue : issueRepository.findAll()) {
            counts.merge(issue.getBook(), 1L, Long::sum);
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toList();
    }

    // ----- Reports -----

    public static class Report {
        public final String title;
        public final String[] headers;
        public final List<String[]> rows;

        public Report(String title, String[] headers, List<String[]> rows) {
            this.title = title;
            this.headers = headers;
            this.rows = rows;
        }
    }

    public Report issuedBooksReport(LocalDate from, LocalDate to) {
        List<BookIssue> issues = issueRepository.findByIssueDateBetween(from, to);
        List<String[]> rows = new ArrayList<>();
        for (BookIssue i : issues) {
            rows.add(new String[]{
                    i.getIssueDate().toString(),
                    i.getBook().getTitle(),
                    i.getBook().getIsbn() != null ? i.getBook().getIsbn() : "-",
                    i.getMember().getFullName(),
                    i.getDueDate().toString(),
                    i.getStatus().getLabel()
            });
        }
        return new Report("Issued Books Report", new String[]{
                "Issue Date", "Book", "ISBN", "Member", "Due Date", "Status"}, rows);
    }

    public Report returnedBooksReport(LocalDate from, LocalDate to) {
        List<BookIssue> issues = issueRepository.findByReturnDateBetween(from, to);
        List<String[]> rows = new ArrayList<>();
        for (BookIssue i : issues) {
            rows.add(new String[]{
                    i.getReturnDate().toString(),
                    i.getBook().getTitle(),
                    i.getMember().getFullName(),
                    i.getIssueDate().toString(),
                    i.getDueDate().toString(),
                    String.format("%.2f", i.getFineAmount())
            });
        }
        return new Report("Returned Books Report", new String[]{
                "Return Date", "Book", "Member", "Issue Date", "Due Date", "Fine"}, rows);
    }

    public Report fineReport(LocalDate from, LocalDate to) {
        List<Fine> fines = fineRepository.findByCreatedAtBetween(
                LocalDateTime.of(from, LocalTime.MIN), LocalDateTime.of(to, LocalTime.MAX));
        List<String[]> rows = new ArrayList<>();
        double total = 0;
        for (Fine f : fines) {
            rows.add(new String[]{
                    f.getCreatedAt().toLocalDate().toString(),
                    f.getMember().getFullName(),
                    f.getIssue() != null ? f.getIssue().getBook().getTitle() : "-",
                    f.getReason() != null ? f.getReason() : "-",
                    String.format("%.2f", f.getAmount()),
                    f.getStatus().getLabel()
            });
            total += f.getAmount();
        }
        return new Report("Fine Report (total $" + String.format("%.2f", total) + ")",
                new String[]{"Date", "Member", "Book", "Reason", "Amount", "Status"}, rows);
    }

    public Report inventoryReport() {
        List<String[]> rows = new ArrayList<>();
        for (Book b : bookRepository.findAll()) {
            rows.add(new String[]{
                    b.getTitle(),
                    b.getAuthor() != null ? b.getAuthor().getName() : "-",
                    b.getCategory() != null ? b.getCategory().getName() : "-",
                    b.getIsbn() != null ? b.getIsbn() : "-",
                    String.valueOf(b.getTotalCopies()),
                    String.valueOf(b.getAvailableCopies()),
                    b.isAvailable() ? "Available" : "Out of stock"
            });
        }
        return new Report("Inventory Report", new String[]{
                "Title", "Author", "Category", "ISBN", "Total", "Available", "Status"}, rows);
    }

    public Report dailyReport(LocalDate day) {
        List<BookIssue> issued = issueRepository.findByIssueDateBetween(day, day);
        List<BookIssue> returned = issueRepository.findByReturnDateBetween(day, day);
        List<String[]> rows = new ArrayList<>();
        for (BookIssue i : issued) {
            rows.add(new String[]{day.toString(), i.getBook().getTitle(),
                    i.getMember().getFullName(), "Issued", ""});
        }
        for (BookIssue i : returned) {
            rows.add(new String[]{day.toString(), i.getBook().getTitle(),
                    i.getMember().getFullName(), "Returned",
                    String.format("%.2f", i.getFineAmount())});
        }
        return new Report("Daily Report - " + day,
                new String[]{"Date", "Book", "Member", "Type", "Fine"}, rows);
    }

    public Report monthlyReport(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        List<String[]> rows = new ArrayList<>();
        for (int i = 1; i <= end.getDayOfMonth(); i++) {
            LocalDate day = start.withDayOfMonth(i);
            long issued = issueRepository.findByIssueDateBetween(day, day).size();
            long returned = issueRepository.findByReturnDateBetween(day, day).size();
            rows.add(new String[]{day.toString(), String.valueOf(issued), String.valueOf(returned)});
        }
        return new Report("Monthly Report - " + year + "-" + String.format("%02d", month),
                new String[]{"Date", "Issued", "Returned"}, rows);
    }
}
