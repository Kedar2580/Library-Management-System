package com.library.service;

import com.library.model.*;
import com.library.repository.BookIssueRepository;
import com.library.repository.BookRepository;
import com.library.repository.FineRepository;
import com.library.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class CirculationService {

    private final BookIssueRepository issueRepository;
    private final BookRepository bookRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final SettingsService settingsService;
    private final NotificationService notificationService;
    private final ActivityService activityService;

    public CirculationService(BookIssueRepository issueRepository, BookRepository bookRepository,
                              FineRepository fineRepository, ReservationRepository reservationRepository,
                              UserService userService, SettingsService settingsService,
                              NotificationService notificationService, ActivityService activityService) {
        this.issueRepository = issueRepository;
        this.bookRepository = bookRepository;
        this.fineRepository = fineRepository;
        this.reservationRepository = reservationRepository;
        this.userService = userService;
        this.settingsService = settingsService;
        this.notificationService = notificationService;
        this.activityService = activityService;
    }

    // ----- Issue -----

    @Transactional
    public String issueBook(Long bookId, Long memberId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        User member = userService.get(memberId);
        if (book == null || member == null) {
            return "Book or member not found.";
        }
        if (!book.isAvailable()) {
            return "This book has no available copies.";
        }
        if (member.getRole() != Role.MEMBER || member.getMembershipStatus() != MembershipStatus.ACTIVE) {
            return "Selected user is not an active member.";
        }
        long activeIssues = issueRepository.findByMember(member).stream()
                .filter(i -> i.getStatus() == IssueStatus.ISSUED || i.isOverdue())
                .count();
        int maxBooks = settingsService.getInt("maxBooksPerMember", 5);
        if (activeIssues >= maxBooks) {
            return "Member has reached the maximum of " + maxBooks + " issued books.";
        }

        int loanDays = settingsService.getInt("loanPeriodDays", 14);
        BookIssue issue = new BookIssue();
        issue.setBook(book);
        issue.setMember(member);
        issue.setIssueDate(LocalDate.now());
        issue.setDueDate(LocalDate.now().plusDays(loanDays));
        issue.setStatus(IssueStatus.ISSUED);
        issueRepository.save(issue);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        notificationService.notify(member, NotificationType.DUE_DATE,
                "Book issued: " + book.getTitle(),
                "Please return \"" + book.getTitle() + "\" by " + issue.getDueDate() + ".");
        activityService.log("Issued \"" + book.getTitle() + "\" to " + member.getFullName());
        return null;
    }

    // ----- Return -----

    @Transactional
    public ReturnResult returnBook(Long issueId) {
        BookIssue issue = issueRepository.findById(issueId).orElse(null);
        if (issue == null || issue.getStatus() == IssueStatus.RETURNED) {
            return new ReturnResult(false, "Issue not found or already returned.", 0);
        }

        issue.setReturnDate(LocalDate.now());
        issue.setStatus(IssueStatus.RETURNED);
        issueRepository.save(issue);

        Book book = issue.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        double fine = 0;
        if (issue.getDueDate().isBefore(issue.getReturnDate())) {
            long days = ChronoUnit.DAYS.between(issue.getDueDate(), issue.getReturnDate());
            double perDay = settingsService.getDouble("finePerDay", 1.0);
            fine = days * perDay;
            Fine f = new Fine();
            f.setIssue(issue);
            f.setMember(issue.getMember());
            f.setAmount(fine);
            f.setReason("Overdue by " + days + " day(s)");
            f.setStatus(FineStatus.PENDING);
            fineRepository.save(f);
            issue.setFineAmount(fine);
            issueRepository.save(issue);
        }

        fulfillReservation(book);
        activityService.log("Returned \"" + book.getTitle() + "\" from " + issue.getMember().getFullName());
        return new ReturnResult(true, fine > 0
                ? "Returned. Fine of $" + String.format("%.2f", fine) + " applies."
                : "Book returned successfully.", fine);
    }

    private void fulfillReservation(Book book) {
        List<Reservation> queue = reservationRepository
                .findByBookAndStatusOrderByReservedAtAsc(book, ReservationStatus.PENDING);
        if (book.getAvailableCopies() > 0 && !queue.isEmpty()) {
            Reservation first = queue.get(0);
            first.setStatus(ReservationStatus.FULFILLED);
            reservationRepository.save(first);
            notificationService.notify(first.getMember(), NotificationType.RESERVATION,
                    "Reserved book now available: " + book.getTitle(),
                    "Your reserved book \"" + book.getTitle() + "\" is now available for borrowing.");
        }
    }

    // ----- Reservation -----

    @Transactional
    public String reserveBook(Long bookId, Long memberId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        User member = userService.get(memberId);
        if (book == null || member == null) {
            return "Book or member not found.";
        }
        if (book.isAvailable()) {
            return "This book is available now, you can borrow it directly.";
        }
        boolean already = reservationRepository
                .findByBookAndMemberAndStatus(book, member, ReservationStatus.PENDING).isPresent();
        if (already) {
            return "You already have a pending reservation for this book.";
        }
        Reservation res = new Reservation();
        res.setBook(book);
        res.setMember(member);
        res.setReservedAt(LocalDateTime.now());
        res.setStatus(ReservationStatus.PENDING);
        reservationRepository.save(res);
        activityService.log(member.getFullName() + " reserved \"" + book.getTitle() + "\"");
        return null;
    }

    @Transactional
    public void cancelReservation(Long id) {
        reservationRepository.findById(id).ifPresent(r -> {
            r.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(r);
        });
    }

    public List<Reservation> allReservations() {
        return reservationRepository.findByStatusOrderByReservedAtAsc(ReservationStatus.PENDING);
    }

    public List<Reservation> memberReservations(User member) {
        return reservationRepository.findByMemberOrderByReservedAtDesc(member);
    }

    // ----- Overdue handling -----

    @Scheduled(cron = "0 0 9 * * *")
    public void dailyOverdueScan() {
        List<BookIssue> overdue = issueRepository
                .findByStatusAndDueDateBefore(IssueStatus.ISSUED, LocalDate.now());
        for (BookIssue issue : overdue) {
            if (issue.getStatus() == IssueStatus.ISSUED) {
                issue.setStatus(IssueStatus.OVERDUE);
                issueRepository.save(issue);
                notificationService.notify(issue.getMember(), NotificationType.OVERDUE,
                        "Book overdue: " + issue.getBook().getTitle(),
                        "Please return \"" + issue.getBook().getTitle()
                                + "\" immediately. Due date was " + issue.getDueDate() + ".");
            }
        }
    }

    @Scheduled(cron = "0 30 8 * * *")
    public void dailyDueDateReminders() {
        LocalDate inThreeDays = LocalDate.now().plusDays(3);
        issueRepository.findByStatus(IssueStatus.ISSUED).stream()
                .filter(i -> i.getDueDate().equals(inThreeDays))
                .forEach(i -> notificationService.notify(i.getMember(), NotificationType.DUE_DATE,
                        "Due date approaching: " + i.getBook().getTitle(),
                        "\"" + i.getBook().getTitle() + "\" is due on " + i.getDueDate() + "."));
    }

    // ----- Queries -----

    public List<BookIssue> activeIssues() {
        List<BookIssue> result = new ArrayList<>(issueRepository.findByStatus(IssueStatus.ISSUED));
        result.addAll(issueRepository.findByStatus(IssueStatus.OVERDUE));
        result.sort((a, b) -> b.getIssueDate().compareTo(a.getIssueDate()));
        return result;
    }

    public List<BookIssue> allIssues() {
        List<BookIssue> list = issueRepository.findAll();
        list.sort((a, b) -> b.getIssueDate().compareTo(a.getIssueDate()));
        return list;
    }

    public List<BookIssue> memberIssues(User member) {
        return issueRepository.findByMemberOrderByIdDesc(member);
    }

    public List<BookIssue> activeIssuesForMember(User member) {
        return issueRepository.findByMember(member).stream()
                .filter(i -> i.getStatus() == IssueStatus.ISSUED || i.getStatus() == IssueStatus.OVERDUE)
                .sorted((a, b) -> b.getIssueDate().compareTo(a.getIssueDate()))
                .toList();
    }

    public double memberPendingFineTotal(User member) {
        return memberFines(member).stream()
                .filter(f -> f.getStatus() == FineStatus.PENDING)
                .mapToDouble(Fine::getAmount)
                .sum();
    }

    public BookIssue getIssue(Long id) {
        return issueRepository.findById(id).orElse(null);
    }

    public List<BookIssue> returnedIssues() {
        return issueRepository.findByStatusOrderByIdDesc(IssueStatus.RETURNED);
    }

    public long overdueCount() {
        return activeIssues().stream().filter(BookIssue::isOverdue).count();
    }

    public long activeCount() {
        return issueRepository.countByStatus(IssueStatus.ISSUED)
                + issueRepository.countByStatus(IssueStatus.OVERDUE);
    }

    public long issuedToday() {
        return issueRepository.findByIssueDateBetween(LocalDate.now(), LocalDate.now()).size();
    }

    public long returnedToday() {
        return issueRepository.findByReturnDateBetween(LocalDate.now(), LocalDate.now()).size();
    }

    // ----- Fines -----

    public List<Fine> pendingFines() {
        return fineRepository.findByStatusOrderByCreatedAtDesc(FineStatus.PENDING);
    }

    public List<Fine> paidFines() {
        return fineRepository.findByStatusOrderByCreatedAtDesc(FineStatus.PAID);
    }

    public List<Fine> memberFines(User member) {
        return fineRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    public void payFine(Long fineId) {
        payFine(fineId, null, null);
    }

    @Transactional
    public void payFine(Long fineId, PaymentMethod method, String reference) {
        fineRepository.findById(fineId).ifPresent(f -> {
            f.setStatus(FineStatus.PAID);
            f.setPaidAt(LocalDateTime.now());
            f.setPaymentMethod(method);
            f.setPaymentReference(reference != null && !reference.isBlank() ? reference.trim() : null);
            fineRepository.save(f);
            activityService.log("Fine of $" + String.format("%.2f", f.getAmount())
                    + " paid by " + f.getMember().getFullName());
            notificationService.notify(f.getMember(), NotificationType.FINE,
                    "Fine paid",
                    "Your fine of $" + String.format("%.2f", f.getAmount())
                            + " has been marked as paid. Thank you!");
        });
    }

    @Transactional
    public int payFines(List<Long> fineIds, PaymentMethod method, String reference) {
        if (fineIds == null || fineIds.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Long id : fineIds) {
            payFine(id, method, reference);
            count++;
        }
        return count;
    }

    @Transactional
    public String createManualFine(Long memberId, double amount, String reason) {
        User member = userService.get(memberId);
        if (member == null) {
            return "Member not found.";
        }
        if (amount <= 0) {
            return "Fine amount must be greater than zero.";
        }
        Fine f = new Fine();
        f.setMember(member);
        f.setAmount(amount);
        f.setReason(reason != null && !reason.isBlank() ? reason.trim() : "Manual fine");
        f.setStatus(FineStatus.PENDING);
        fineRepository.save(f);
        activityService.log("Fine of $" + String.format("%.2f", amount)
                + " added for " + member.getFullName());
        notificationService.notify(member, NotificationType.FINE,
                "Fine added", "A fine of $" + String.format("%.2f", amount) + " has been added to your account.");
        return null;
    }

    public double pendingFineTotal() {
        return fineRepository.sumAmountByStatus(FineStatus.PENDING);
    }

    public double paidFineTotal() {
        return fineRepository.sumAmountByStatus(FineStatus.PAID);
    }

    public record ReturnResult(boolean success, String message, double fine) {
    }
}
