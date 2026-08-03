package com.library.service;

import com.library.model.Book;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("title", "isbn", "author", "category", "publisher",
                    "year", "totalCopies", "shelfLocation")
            .build();

    public String exportBooks(List<Book> books) {
        StringWriter out = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(out, FORMAT)) {
            for (Book b : books) {
                printer.printRecord(
                        b.getTitle(),
                        b.getIsbn(),
                        b.getAuthor() != null ? b.getAuthor().getName() : "",
                        b.getCategory() != null ? b.getCategory().getName() : "",
                        b.getPublisher() != null ? b.getPublisher().getName() : "",
                        b.getPublicationYear(),
                        b.getTotalCopies(),
                        b.getShelfLocation());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }

    public List<String[]> parse(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        Iterable<CSVRecord> records = FORMAT.parse(new StringReader(new String(file.getBytes())));
        for (CSVRecord rec : records) {
            if (rec.getRecordNumber() == 1) {
                continue;
            }
            rows.add(new String[]{
                    rec.get("title"),
                    rec.get("isbn"),
                    rec.get("author"),
                    rec.get("category"),
                    rec.get("publisher"),
                    rec.get("year"),
                    rec.get("totalCopies"),
                    rec.get("shelfLocation")
            });
        }
        return rows;
    }
}
