package utils;

import database.DatabaseManager;
import models.Book;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {
    private DatabaseManager dbManager;

    public CSVImporter(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public ImportResult importFromCSV(String filePath) {
        ImportResult result = new ImportResult();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            // Skip header
            String line = br.readLine();
            if (line != null && line.startsWith("\uFEFF")) {
                line = line.substring(1); // Remove BOM if exists
            }

            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;

                try {
                    Book book = parseCSVLine(line);
                    if (book != null) {
                        if (dbManager.addBook(book)) {
                            result.successCount++;
                        } else {
                            result.failedCount++;
                            result.errors.add("Dòng " + lineNumber + ": Lỗi khi lưu vào database");
                        }
                    }
                } catch (Exception e) {
                    result.failedCount++;
                    result.errors.add("Dòng " + lineNumber + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            result.errors.add("Lỗi đọc file: " + e.getMessage());
        }

        return result;
    }

    private Book parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());

        if (values.size() < 6) {
            throw new IllegalArgumentException("Dữ liệu không đủ cột");
        }

        Book book = new Book();
        book.setTitle(values.get(0));
        book.setAuthor(values.get(1));
        book.setCategory(values.get(2));
        book.setDescription(values.get(3));
        book.setContent(values.get(4));

        try {
            book.setYear(Integer.parseInt(values.get(5)));
        } catch (NumberFormatException e) {
            book.setYear(2024);
        }

        return book;
    }

    public static class ImportResult {
        public int successCount = 0;
        public int failedCount = 0;
        public List<String> errors = new ArrayList<>();

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Thành công: %d\n", successCount));
            sb.append(String.format("Thất bại: %d\n", failedCount));

            if (!errors.isEmpty()) {
                sb.append("\nChi tiết lỗi:\n");
                for (String error : errors) {
                    sb.append("- ").append(error).append("\n");
                }
            }

            return sb.toString();
        }
    }
}
