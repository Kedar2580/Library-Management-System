package com.library.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class BackupService {

    private final JdbcTemplate jdbcTemplate;

    public BackupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Path createBackup() throws IOException {
        Path backupDir = Path.of("backups");
        Files.createDirectories(backupDir);
        Path file = backupDir.resolve("library-backup-"
                + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter
                .ofPattern("yyyyMMdd-HHmmss")) + ".sql");
        jdbcTemplate.execute("SCRIPT TO '" + file.toAbsolutePath().toString().replace("'", "''") + "'");
        return file;
    }

    public void restore(Path file) throws IOException {
        jdbcTemplate.execute("DROP ALL OBJECTS");
        jdbcTemplate.execute("RUNSCRIPT FROM '" + file.toAbsolutePath().toString().replace("'", "''") + "'");
    }
}
