package com.library.service;

import com.library.model.Book;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SUB_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);

    public byte[] catalogPdf(List<Book> books) {
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("Library Catalog", TITLE_FONT));
            doc.add(new Paragraph("Complete book list grouped by category - " + LocalDate.now(), BODY_FONT));
            doc.add(new Paragraph(" ", BODY_FONT));

            Map<String, List<Book>> byCategory = new LinkedHashMap<>();
            for (Book b : books) {
                String cat = b.getCategory() != null ? b.getCategory().getName() : "Uncategorized";
                byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(b);
            }

            for (Map.Entry<String, List<Book>> entry : byCategory.entrySet()) {
                doc.add(new Paragraph(entry.getKey(), SUB_FONT));
                doc.add(new Paragraph(entry.getValue().size() + " books", BODY_FONT));
                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setSpacingBefore(6);
                table.setSpacingAfter(16);
                addHeader(table, "Title", "Author", "ISBN", "Year", "Copies", "Available");
                for (Book b : entry.getValue()) {
                    table.addCell(cell(b.getTitle()));
                    table.addCell(cell(b.getAuthor() != null ? b.getAuthor().getName() : "-"));
                    table.addCell(cell(b.getIsbn() != null ? b.getIsbn() : "-"));
                    table.addCell(cell(String.valueOf(b.getPublicationYear())));
                    table.addCell(cell(String.valueOf(b.getTotalCopies())));
                    table.addCell(cell(String.valueOf(b.getAvailableCopies())));
                }
                doc.add(table);
            }
            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to build catalog PDF", e);
        }
        return out.toByteArray();
    }

    public byte[] reportPdf(ReportService.Report report) {
        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph(report.title, TITLE_FONT));
            doc.add(new Paragraph("Generated on " + LocalDate.now(), BODY_FONT));
            doc.add(new Paragraph(" ", BODY_FONT));

            PdfPTable table = new PdfPTable(report.headers.length);
            table.setWidthPercentage(100);
            addHeader(table, report.headers);
            for (String[] row : report.rows) {
                for (String cellValue : row) {
                    table.addCell(cell(cellValue));
                }
            }
            doc.add(table);
            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to build report PDF", e);
        }
        return out.toByteArray();
    }

    private void addHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, HEADER_FONT));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setGrayFill(0.9f);
            table.addCell(c);
        }
    }

    private PdfPCell cell(String value) {
        PdfPCell c = new PdfPCell(new Phrase(value == null ? "" : value, BODY_FONT));
        c.setPadding(4);
        return c;
    }
}
