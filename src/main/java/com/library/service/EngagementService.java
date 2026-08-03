package com.library.service;

import com.library.model.*;
import com.library.repository.BookRepository;
import com.library.repository.MessageRepository;
import com.library.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EngagementService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final MessageRepository messageRepository;
    private final ActivityService activityService;

    public EngagementService(ReviewRepository reviewRepository, BookRepository bookRepository,
                             MessageRepository messageRepository, ActivityService activityService) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.messageRepository = messageRepository;
        this.activityService = activityService;
    }

    @Transactional
    public Review addReview(Long bookId, User user, int rating, String comment) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return null;
        }
        Review review = new Review();
        review.setBook(book);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        review = reviewRepository.save(review);

        List<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        book.setAvgRating(sum / reviews.size());
        book.setReviewCount(reviews.size());
        bookRepository.save(book);
        return review;
    }

    public List<Review> reviewsForBook(Long bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }

    public List<Review> reviewsByUser(User user) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public List<Review> allReviews() {
        return reviewRepository.findAll();
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    // ----- Feedback / suggestions / contact -----

    public Message submitMessage(String name, String email, String subject, String content, MessageType type) {
        Message m = new Message();
        m.setName(name);
        m.setEmail(email);
        m.setSubject(subject);
        m.setContent(content);
        m.setType(type);
        m.setStatus(MessageStatus.NEW);
        m = messageRepository.save(m);
        activityService.log("New " + type.getLabel().toLowerCase() + " received: " + subject);
        return m;
    }

    public List<Message> allMessages() {
        return messageRepository.findByOrderByCreatedAtDesc();
    }

    public List<Message> newMessages() {
        return messageRepository.findByStatusOrderByCreatedAtDesc(MessageStatus.NEW);
    }

    public Message getMessage(Long id) {
        return messageRepository.findById(id).orElse(null);
    }

    public void setMessageStatus(Long id, MessageStatus status) {
        messageRepository.findById(id).ifPresent(m -> {
            m.setStatus(status);
            messageRepository.save(m);
        });
    }

    public long newMessagesCount() {
        return messageRepository.countByStatus(MessageStatus.NEW);
    }
}
