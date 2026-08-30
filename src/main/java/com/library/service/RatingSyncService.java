package com.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.model.Book;
import com.library.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetches real-world average ratings (0-5) and review counts for books from the
 * OpenLibrary API (free, no API key) and stores them on the Book entity.
 */
@Service
public class RatingSyncService {

    private static final Logger log = LoggerFactory.getLogger(RatingSyncService.class);
    private static final String USER_AGENT = "CommunityLibrary/1.0 (contact library@example.com)";
    private static final int RATING_ATTEMPTS = 6;

    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public RatingSyncService(BookRepository bookRepository, ObjectMapper objectMapper) {
        this.bookRepository = bookRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Fetches ratings for every book in the catalog from OpenLibrary and stores them.
     * Runs in two passes: first resolves each ISBN to a work key (reliable lookup),
     * then fetches the rating for each work key (intermittent, so retried per book).
     * Returns a short summary of results.
     */
    public String syncAll() {
        List<Book> books = bookRepository.findAll();

        Map<Long, String> workKeys = new HashMap<>();
        int resolved = 0;
        for (Book book : books) {
            String workKey = resolveWorkKey(book.getIsbn());
            if (workKey != null) {
                workKeys.put(book.getId(), workKey);
                resolved++;
            }
        }
        log.info("Rating sync: resolved {} of {} work keys", resolved, books.size());

        int updated = 0;
        int failed = 0;
        for (Book book : books) {
            String workKey = workKeys.get(book.getId());
            if (workKey == null) {
                failed++;
                continue;
            }
            Rating rating = fetchRatings(workKey);
            if (rating != null) {
                applyRating(book, rating);
                updated++;
            } else {
                failed++;
            }
        }
        log.info("Rating sync complete: {} updated, {} failed (of {})", updated, failed, books.size());
        return "Ratings synced for " + updated + " of " + books.size() + " books."
                + (failed > 0 ? " " + failed + " not found (offline or no rating)." : "");
    }

    /**
     * Loads internet rating for a single book, converting OpenLibrary's 0-5 scale.
     */
    public Rating fetchRating(Book book) {
        String workKey = resolveWorkKey(book.getIsbn());
        if (workKey == null) {
            return null;
        }
        return fetchRatings(workKey);
    }

    private void applyRating(Book book, Rating rating) {
        book.setAvgRating(rating.average());
        book.setReviewCount(rating.count());
        bookRepository.save(book);
    }

    @Transactional
    public void applyRatingTx(Book book, Rating rating) {
        applyRating(book, rating);
    }

    /**
     * Resolves an ISBN to an OpenLibrary work key via the edition record.
     */
    private String resolveWorkKey(String isbn) {
        String normalized = normalizeIsbn(isbn);
        if (normalized == null) {
            return null;
        }
        String body = get("https://openlibrary.org/isbn/" + normalized + ".json");
        if (body == null) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            JsonNode works = node.get("works");
            if (works != null && works.isArray() && works.size() > 0) {
                return works.get(0).get("key").asText();
            }
        } catch (IOException e) {
            log.debug("Could not parse edition for ISBN {}", normalized, e);
        }
        return null;
    }

    private Rating fetchRatings(String workKey) {
        final String url = "https://openlibrary.org" + workKey + "/ratings.json";
        for (int attempt = 0; attempt < RATING_ATTEMPTS; attempt++) {
            String body = get(url);
            if (body == null) {
                sleep(2000L);
                continue;
            }
            try {
                JsonNode node = objectMapper.readTree(body);
                JsonNode summary = node.get("summary");
                if (summary != null && summary.has("average") && summary.has("count")) {
                    double average = summary.get("average").asDouble();
                    int count = summary.get("count").asInt();
                    if (count > 0) {
                        return new Rating(average, count);
                    }
                }
            } catch (IOException e) {
                log.debug("Could not parse ratings for {}", workKey, e);
            }
            break;
        }
        return null;
    }

    private String get(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.debug("Request failed for {}", url, e);
        }
        return null;
    }

    private static String normalizeIsbn(String isbn) {
        if (isbn == null) {
            return null;
        }
        String digits = isbn.replaceAll("[^0-9Xx]", "").toUpperCase();
        return digits;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public record Rating(double average, int count) {
    }
}