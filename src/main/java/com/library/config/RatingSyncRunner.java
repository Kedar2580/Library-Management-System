package com.library.config;

import com.library.service.RatingSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Fetches real-world book ratings from the internet shortly after startup,
 * on a background thread so the app is available immediately.
 */
@Component
public class RatingSyncRunner {

    private static final Logger log = LoggerFactory.getLogger(RatingSyncRunner.class);

    private final RatingSyncService ratingSyncService;

    public RatingSyncRunner(RatingSyncService ratingSyncService) {
        this.ratingSyncService = ratingSyncService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        Thread worker = new Thread(() -> {
            try {
                log.info("Starting background internet rating sync...");
                String result = ratingSyncService.syncAll();
                log.info("Startup rating sync finished: {}", result);
            } catch (Exception e) {
                log.warn("Startup rating sync failed", e);
            }
        }, "rating-sync-worker");
        worker.setDaemon(true);
        worker.start();
    }
}
