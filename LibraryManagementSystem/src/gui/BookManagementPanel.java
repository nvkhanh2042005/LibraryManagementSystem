package gui;

import database.DatabaseManager;
import models.Book;
import utils.CSVImporter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class BookManagementPanel extends JPanel {
    private DatabaseManager dbManager;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField txtTitle, txtAuthor, txtCategory, txtYear, txtSearch;
    private JTextArea txtDescription, txtContent;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch, btnImport, btnExport;
    private int selectedBookId = -1;

    public BookManagementPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        initComponents();
        loadBooks();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel nhập liệu
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.NORTH);

        // Panel bảng dữ liệu
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Thông tin Sách"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("Tên sách:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        txtTitle = new JTextField(30);
        panel.add(txtTitle, gbc);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(new JLabel("Tác giả:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        txtAuthor = new JTextField(20);
        panel.add(txtAuthor, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(new JLabel("Thể loại:"), gbc);

        gbc.gridx = 3; gbc.weightx = 0.5;
        txtCategory = new JTextField(15);
        panel.add(txtCategory, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(new JLabel("Năm xuất bản:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0;
        txtYear = new JTextField(10);
        panel.add(txtYear, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Mô tả:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        txtDescription = new JTextArea(3, 30);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        panel.add(scrollDesc, gbc);

        // Row 4
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Nội dung:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3;
        txtContent = new JTextArea(5, 30);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        JScrollPane scrollContent = new JScrollPane(txtContent);
        panel.add(scrollContent, gbc);

        // Row 5 - Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());

        btnAdd = new JButton("Thêm");
        btnUpdate = new JButton("Cập nhật");
        btnDelete = new JButton("Xóa");
        btnClear = new JButton("Làm mới");
        btnImport = new JButton("Import CSV");
        btnExport = new JButton("Export CSV");

        btnAdd.addActionListener(e -> addBook());
        btnUpdate.addActionListener(e -> updateBook());
        btnDelete.addActionListener(e -> deleteBook());
        btnClear.addActionListener(e -> clearForm());
        btnImport.addActionListener(e -> importFromCSV());
        btnExport.addActionListener(e -> exportToCSV());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnImport);
        buttonPanel.add(btnExport);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Danh sách Sách"));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm:"));
        txtSearch = new JTextField(30);
        txtSearch.addActionListener(e -> searchBooks());
        btnSearch = new JButton("Tìm");
        btnSearch.addActionListener(e -> searchBooks());
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        JButton btnShowAll = new JButton("Hiển thị tất cả");
        btnShowAll.addActionListener(e -> loadBooks());
        searchPanel.add(btnShowAll);

        JLabel lblCount = new JLabel();
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(lblCount);

        panel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Tên sách", "Tác giả", "Thể loại", "Năm", "Cụm"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setRowHeight(25);
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedBook();
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Update count label
        updateCountLabel(lblCount);

        return panel;
    }

    private void updateCountLabel(JLabel label) {
        int count = tableModel.getRowCount();
        label.setText(String.format("Tổng: %d sách", count));
    }

    private void addBook() {
        try {
            Book book = new Book();
            book.setTitle(txtTitle.getText().trim());
            book.setAuthor(txtAuthor.getText().trim());
            book.setCategory(txtCategory.getText().trim());
            book.setDescription(txtDescription.getText().trim());
            book.setContent(txtContent.getText().trim());

            String yearText = txtYear.getText().trim();
            if (yearText.isEmpty()) {
                book.setYear(2024);
            } else {
                book.setYear(Integer.parseInt(yearText));
            }

            if (book.getTitle().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập tên sách!",
                        "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
                txtTitle.requestFocus();
                return;
            }

            if (book.getAuthor().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập tác giả!",
                        "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
                txtAuthor.requestFocus();
                return;
            }

            if (dbManager.addBook(book)) {
                JOptionPane.showMessageDialog(this,
                        "Thêm sách thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                loadBooks();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi thêm sách vào database!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Năm xuất bản không hợp lệ! Vui lòng nhập số.",
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
            txtYear.requestFocus();
            txtYear.selectAll();
        }
    }

    private void updateBook() {
        if (selectedBookId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn sách cần cập nhật từ bảng!",
                    "Chưa chọn sách",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Book book = new Book();
            book.setId(selectedBookId);
            book.setTitle(txtTitle.getText().trim());
            book.setAuthor(txtAuthor.getText().trim());
            book.setCategory(txtCategory.getText().trim());
            book.setDescription(txtDescription.getText().trim());
            book.setContent(txtContent.getText().trim());

            String yearText = txtYear.getText().trim();
            if (yearText.isEmpty()) {
                book.setYear(2024);
            } else {
                book.setYear(Integer.parseInt(yearText));
            }

            if (book.getTitle().isEmpty() || book.getAuthor().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Tên sách và tác giả không được để trống!",
                        "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (dbManager.updateBook(book)) {
                JOptionPane.showMessageDialog(this,
                        "Cập nhật thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                loadBooks();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi cập nhật!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Năm xuất bản không hợp lệ!",
                    "Lỗi dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
            txtYear.requestFocus();
            txtYear.selectAll();
        }
    }

    private void deleteBook() {
        if (selectedBookId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn sách cần xóa từ bảng!",
                    "Chưa chọn sách",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Book book = dbManager.getBookById(selectedBookId);
        if (book == null) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy sách!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                String.format("Bạn có chắc muốn xóa sách:\n\"%s\"?", book.getTitle()),
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            if (dbManager.deleteBook(selectedBookId)) {
                JOptionPane.showMessageDialog(this,
                        "Xóa thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                loadBooks();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xóa sách!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedBookId = -1;
        txtTitle.setText("");
        txtAuthor.setText("");
        txtCategory.setText("");
        txtYear.setText("");
        txtDescription.setText("");
        txtContent.setText("");
        bookTable.clearSelection();
    }

    public void loadBooks() {
        tableModel.setRowCount(0);
        List<Book> books = dbManager.getAllBooks();

        for (Book book : books) {
            Object[] row = {
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getYear(),
                    book.getClusterId() >= 0 ? "Cụm " + (book.getClusterId() + 1) : "Chưa phân cụm"
            };
            tableModel.addRow(row);
        }
    }

    private void searchBooks() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadBooks();
            return;
        }

        tableModel.setRowCount(0);
        List<Book> books = dbManager.searchBooks(keyword);

        if (books.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    String.format("Không tìm thấy sách nào với từ khóa: \"%s\"", keyword),
                    "Kết quả tìm kiếm",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (Book book : books) {
            Object[] row = {
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getYear(),
                    book.getClusterId() >= 0 ? "Cụm " + (book.getClusterId() + 1) : "Chưa phân cụm"
            };
            tableModel.addRow(row);
        }

        JOptionPane.showMessageDialog(this,
                String.format("Tìm thấy %d kết quả", books.size()),
                "Kết quả tìm kiếm",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedBookId = (int) tableModel.getValueAt(selectedRow, 0);
            Book book = dbManager.getBookById(selectedBookId);

            if (book != null) {
                txtTitle.setText(book.getTitle());
                txtAuthor.setText(book.getAuthor());
                txtCategory.setText(book.getCategory());
                txtYear.setText(String.valueOf(book.getYear()));
                txtDescription.setText(book.getDescription());
                txtContent.setText(book.getContent());
            }
        }
    }

    private void importFromCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file CSV để import");
        fileChooser.setCurrentDirectory(new File("data"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CSV Files (*.csv)", "csv"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Confirm before import
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Bạn có muốn import dữ liệu từ file:\n%s?", selectedFile.getName()),
                    "Xác nhận Import",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Show progress dialog
            JDialog progressDialog = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Đang import dữ liệu...",
                    false);
            JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
            progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel lblStatus = new JLabel("Đang đọc file CSV...");
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);

            progressPanel.add(lblStatus, BorderLayout.NORTH);
            progressPanel.add(progressBar, BorderLayout.CENTER);

            progressDialog.add(progressPanel);
            progressDialog.setSize(400, 120);
            progressDialog.setLocationRelativeTo(this);

            SwingWorker<CSVImporter.ImportResult, String> worker = new SwingWorker<>() {
                @Override
                protected CSVImporter.ImportResult doInBackground() {
                    publish("Đang đọc và phân tích file...");
                    CSVImporter importer = new CSVImporter(dbManager);
                    return importer.importFromCSV(selectedFile.getAbsolutePath());
                }

                @Override
                protected void process(List<String> chunks) {
                    if (!chunks.isEmpty()) {
                        lblStatus.setText(chunks.get(chunks.size() - 1));
                    }
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        CSVImporter.ImportResult importResult = get();

                        // Show result dialog
                        JDialog resultDialog = new JDialog(
                                (Frame) SwingUtilities.getWindowAncestor(BookManagementPanel.this),
                                "Kết quả Import",
                                true);
                        resultDialog.setLayout(new BorderLayout(10, 10));

                        JTextArea txtResult = new JTextArea(15, 50);
                        txtResult.setEditable(false);
                        txtResult.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        txtResult.setText(importResult.getSummary());
                        txtResult.setCaretPosition(0);

                        JScrollPane scrollPane = new JScrollPane(txtResult);
                        resultDialog.add(scrollPane, BorderLayout.CENTER);

                        JButton btnClose = new JButton("Đóng");
                        btnClose.addActionListener(e -> resultDialog.dispose());
                        JPanel buttonPanel = new JPanel();
                        buttonPanel.add(btnClose);
                        resultDialog.add(buttonPanel, BorderLayout.SOUTH);

                        resultDialog.setSize(600, 400);
                        resultDialog.setLocationRelativeTo(BookManagementPanel.this);
                        resultDialog.setVisible(true);

                        loadBooks();

                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(BookManagementPanel.this,
                                "Lỗi: " + e.getMessage(),
                                "Lỗi Import",
                                JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            };

            worker.execute();
            progressDialog.setVisible(true);
        }
    }

    private void exportToCSV() {
        List<Book> books = dbManager.getAllBooks();

        if (books.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có dữ liệu để export!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file CSV");
        fileChooser.setCurrentDirectory(new File("data"));
        fileChooser.setSelectedFile(new File("books_export.csv"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CSV Files (*.csv)", "csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Add .csv extension if not present
            if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
            }

            // Check if file exists
            if (selectedFile.exists()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "File đã tồn tại. Bạn có muốn ghi đè?",
                        "Xác nhận",
                        JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            File finalFile = selectedFile;
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    try (java.io.PrintWriter writer = new java.io.PrintWriter(
                            new java.io.OutputStreamWriter(
                                    new java.io.FileOutputStream(finalFile),
                                    java.nio.charset.StandardCharsets.UTF_8))) {

                        // Write BOM for UTF-8
                        writer.write("\uFEFF");

                        // Write header
                        writer.println("title,author,category,description,content,year");

                        // Write data
                        for (Book book : books) {
                            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d%n",
                                    escapeCSV(book.getTitle()),
                                    escapeCSV(book.getAuthor()),
                                    escapeCSV(book.getCategory()),
                                    escapeCSV(book.getDescription()),
                                    escapeCSV(book.getContent()),
                                    book.getYear());
                        }

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(BookManagementPanel.this,
                                    String.format("Export thành công %d sách vào file:\n%s",
                                            books.size(), finalFile.getAbsolutePath()),
                                    "Thành công",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(BookManagementPanel.this,
                                    "Lỗi khi export dữ liệu!",
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(BookManagementPanel.this,
                                "Lỗi: " + e.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
