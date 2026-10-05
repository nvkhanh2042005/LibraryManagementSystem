package gui;

import algorithms.*;
import database.DatabaseManager;
import models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ClusteringPanel extends JPanel {
    private DatabaseManager dbManager;
    private JSpinner spinnerK;
    private JSpinner spinnerMaxIterations;
    private JButton btnCluster;
    private JTextArea txtLog;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;
    private List<Cluster> clusters;
    private List<Document> processedDocuments;

    public ClusteringPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.processedDocuments = new ArrayList<>();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel cài đặt
        JPanel settingsPanel = createSettingsPanel();
        add(settingsPanel, BorderLayout.NORTH);

        // Panel kết quả
        JPanel resultPanel = createResultPanel();
        add(resultPanel, BorderLayout.CENTER);

        // Panel log
        JPanel logPanel = createLogPanel();
        add(logPanel, BorderLayout.SOUTH);
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Cài đặt K-Means"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Số cụm K
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Số cụm (K):"), gbc);

        gbc.gridx = 1;
        spinnerK = new JSpinner(new SpinnerNumberModel(4, 2, 10, 1));
        panel.add(spinnerK, gbc);

        // Số vòng lặp tối đa
        gbc.gridx = 2;
        panel.add(new JLabel("Số vòng lặp tối đa:"), gbc);

        gbc.gridx = 3;
        spinnerMaxIterations = new JSpinner(new SpinnerNumberModel(100, 10, 500, 10));
        panel.add(spinnerMaxIterations, gbc);

        // Nút thực hiện
        gbc.gridx = 4;
        btnCluster = new JButton("Phân Cụm");
        btnCluster.addActionListener(e -> startClustering());
        panel.add(btnCluster, gbc);

        // Progress bar
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 5;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        panel.add(progressBar, gbc);

        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Kết quả Phân cụm (Double-click để xem chi tiết)"));

        String[] columns = {"Cụm", "Số tài liệu", "Tài liệu"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultTable = new JTable(tableModel);
        resultTable.setRowHeight(25);
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(600);

        // Thêm double-click listener để hiển thị chi tiết
        resultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    int row = resultTable.getSelectedRow();
                    if (row >= 0 && clusters != null && row < clusters.size()) {
                        showClusterDetails(clusters.get(row));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Nút xuất kết quả
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExport = new JButton("Xuất Kết Quả");
        btnExport.addActionListener(e -> exportResults());
        JButton btnSaveToDatabase = new JButton("Lưu vào Database");
        btnSaveToDatabase.addActionListener(e -> saveToDatabase());
        buttonPanel.add(btnExport);
        buttonPanel.add(btnSaveToDatabase);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Log Quá trình"));

        txtLog = new JTextArea(8, 50);
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(txtLog);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton btnClearLog = new JButton("Xóa Log");
        btnClearLog.addActionListener(e -> txtLog.setText(""));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnClearLog);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void startClustering() {
        int k = (Integer) spinnerK.getValue();
        int maxIterations = (Integer) spinnerMaxIterations.getValue();

        // Kiểm tra dữ liệu
        List<Book> books = dbManager.getAllBooks();
        if (books.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có sách trong database!\nVui lòng import dữ liệu trước.",
                    "Không có dữ liệu",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (books.size() < k) {
            JOptionPane.showMessageDialog(this,
                    String.format("Số lượng sách (%d) phải >= số cụm K (%d)!", books.size(), k),
                    "Dữ liệu không đủ",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Xóa kết quả cũ
        tableModel.setRowCount(0);
        txtLog.setText("");

        // Chạy clustering trong background thread
        SwingWorker<List<Cluster>, String> worker = new SwingWorker<>() {
            @Override
            protected List<Cluster> doInBackground() throws Exception {
                return performClustering(books, k, maxIterations);
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    txtLog.append(message + "\n");
                    txtLog.setCaretPosition(txtLog.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                btnCluster.setEnabled(true);

                try {
                    clusters = get();
                    if (clusters != null) {
                        displayResults(clusters, books);
                        publish("\n✓ Hoàn thành phân cụm!");
                        JOptionPane.showMessageDialog(ClusteringPanel.this,
                                "Phân cụm hoàn thành!\n\nDouble-click vào cụm để xem chi tiết.",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    publish("\n✗ LỖI: " + e.getMessage());
                    JOptionPane.showMessageDialog(ClusteringPanel.this,
                            "Lỗi khi phân cụm: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };

        btnCluster.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        worker.execute();
    }

    private List<Cluster> performClustering(List<Book> books, int k, int maxIterations) {
        try {
            // 1. Load data
            publish("=== BẮT ĐẦU PHÂN CỤM K-MEANS ===");
            publish(String.format("Số lượng sách: %d", books.size()));
            publish(String.format("Số cụm K: %d", k));
            publish(String.format("Số vòng lặp tối đa: %d", maxIterations));

            // 2. Preprocessing
            publish("\n[1] Tiền xử lý văn bản...");
            TextPreprocessor preprocessor = new TextPreprocessor();
            List<Document> documents = new ArrayList<>();

            for (Book book : books) {
                // Sử dụng extractKeywords với trọng số cao cho title và category
                String[] processedTerms = preprocessor.extractKeywords(
                        book.getTitle(),
                        book.getCategory(),
                        book.getDescription() + " " + book.getContent()
                );

                if (processedTerms.length > 0) {
                    Document doc = new Document(book.getId(), book.getFullText(), processedTerms);
                    documents.add(doc);
                } else {
                    publish("   ⚠ Bỏ qua sách: " + book.getTitle() + " (không có từ khóa)");
                }
            }

            publish(String.format("   ✓ Xử lý thành công %d tài liệu", documents.size()));

            if (documents.size() < k) {
                publish("   ✗ LỖI: Không đủ tài liệu để phân cụm!");
                return null;
            }

            // 3. Calculate TF-IDF
            publish("\n[2] Tính toán TF-IDF...");
            TFIDFCalculator tfidfCalc = new TFIDFCalculator();
            for (Document doc : documents) {
                tfidfCalc.addDocument(doc);
            }
            tfidfCalc.calculateTFIDF();
            publish(String.format("   ✓ Kích thước từ vựng: %d từ", tfidfCalc.getVocabularySize()));

            // Lưu documents để sử dụng sau
            this.processedDocuments = new ArrayList<>(documents);

            // Debug: Kiểm tra TF-IDF vectors
            publish("\n[DEBUG] Thông tin TF-IDF vectors:");
            int totalTerms = 0;
            int minTerms = Integer.MAX_VALUE;
            int maxTerms = 0;

            for (Document doc : documents) {
                int terms = doc.getTfidfVector().size();
                totalTerms += terms;
                minTerms = Math.min(minTerms, terms);
                maxTerms = Math.max(maxTerms, terms);
            }

            double avgTerms = totalTerms / (double) documents.size();
            publish(String.format("   Trung bình: %.1f terms/doc", avgTerms));
            publish(String.format("   Min: %d, Max: %d terms", minTerms, maxTerms));

            // Kiểm tra similarity giữa 2 docs đầu
            if (documents.size() >= 2) {
                double sim = CosineSimilarity.calculate(
                        documents.get(0).getTfidfVector(),
                        documents.get(1).getTfidfVector()
                );
                publish(String.format("   Similarity mẫu (doc 1-2): %.4f", sim));
            }

            // 4. K-Means Clustering
            publish("\n[3] Thực hiện K-Means clustering...");
            KMeansClustering kmeans = new KMeansClustering(k, maxIterations);
            List<Cluster> clusters = kmeans.cluster(documents);

            // 5. Summary
            publish("\n[4] Tóm tắt kết quả:");
            for (Cluster cluster : clusters) {
                publish(String.format("   Cụm %d: %d tài liệu",
                        cluster.getId() + 1, cluster.size()));
            }

            // Debug: In một số sách mẫu từ mỗi cụm
            publish("\n[DEBUG] Sách mẫu trong từng cụm:");
            for (Cluster cluster : clusters) {
                publish(String.format("\n   Cụm %d:", cluster.getId() + 1));
                int count = 0;
                for (int bookId : cluster.getBookIds()) {
                    if (count >= 3) break;
                    Book book = books.stream()
                            .filter(b -> b.getId() == bookId)
                            .findFirst()
                            .orElse(null);
                    if (book != null) {
                        publish(String.format("     - %s (%s)",
                                book.getTitle(), book.getCategory()));
                    }
                    count++;
                }
            }

            return clusters;

        } catch (Exception e) {
            publish("\n✗ LỖI: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void displayResults(List<Cluster> clusters, List<Book> books) {
        tableModel.setRowCount(0);

        for (Cluster cluster : clusters) {
            StringBuilder bookTitles = new StringBuilder();
            List<String> titles = new ArrayList<>();

            for (int bookId : cluster.getBookIds()) {
                Book book = books.stream()
                        .filter(b -> b.getId() == bookId)
                        .findFirst()
                        .orElse(null);

                if (book != null) {
                    titles.add(book.getTitle());
                }
            }

            // Giới hạn hiển thị 5 sách đầu
            int displayCount = Math.min(5, titles.size());
            for (int i = 0; i < displayCount; i++) {
                if (i > 0) bookTitles.append("; ");
                bookTitles.append(titles.get(i));
            }

            if (titles.size() > displayCount) {
                bookTitles.append(String.format("; ... (%d sách khác)",
                        titles.size() - displayCount));
            }

            Object[] row = {
                    "Cụm " + (cluster.getId() + 1),
                    cluster.size(),
                    bookTitles.toString()
            };
            tableModel.addRow(row);
        }
    }

    private void showClusterDetails(Cluster cluster) {
        // Tạo dialog
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Chi tiết Cụm " + (cluster.getId() + 1),
                true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Main panel với padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header panel - Thông tin tổng quan
        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Thông tin cụm"));

        JLabel lblSize = new JLabel(String.format("Số lượng: %d sách", cluster.size()));
        lblSize.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(lblSize);

        JLabel lblClusterId = new JLabel(String.format("ID Cụm: %d", cluster.getId()));
        headerPanel.add(lblClusterId);

        // Tính silhouette score cho cụm này
        double silhouette = calculateClusterSilhouette(cluster);
        JLabel lblSilhouette = new JLabel(String.format("Silhouette Score: %.3f", silhouette));
        headerPanel.add(lblSilhouette);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel - Danh sách sách
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Danh sách sách"));

        List<Book> books = dbManager.getAllBooks();
        DefaultListModel<String> listModel = new DefaultListModel<>();

        int index = 1;
        for (int bookId : cluster.getBookIds()) {
            Book book = books.stream()
                    .filter(b -> b.getId() == bookId)
                    .findFirst()
                    .orElse(null);

            if (book != null) {
                listModel.addElement(String.format("%d. %s - %s (%d)",
                        index++,
                        book.getTitle(),
                        book.getAuthor(),
                        book.getYear()));
            }
        }

        JList<String> bookList = new JList<>(listModel);
        bookList.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(bookList);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel - Từ khóa đặc trưng
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Từ khóa đặc trưng"));

        Map<String, Double> topKeywords = getTopKeywordsForCluster(cluster, 15);
        JTextArea keywordsArea = new JTextArea(4, 50);
        keywordsArea.setText(formatKeywords(topKeywords));
        keywordsArea.setEditable(false);
        keywordsArea.setLineWrap(true);
        keywordsArea.setWrapStyleWord(true);
        keywordsArea.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane keywordsScroll = new JScrollPane(keywordsArea);
        bottomPanel.add(keywordsScroll, BorderLayout.CENTER);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        buttonPanel.add(btnClose);

        // Add panels to dialog
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private double calculateClusterSilhouette(Cluster cluster) {
        if (cluster.size() <= 1 || processedDocuments.isEmpty()) {
            return 0.0;
        }

        List<Document> clusterDocs = new ArrayList<>();

        // Lấy tất cả documents trong cluster này
        for (int bookId : cluster.getBookIds()) {
            for (Document doc : processedDocuments) {
                if (doc.getBookId() == bookId) {
                    clusterDocs.add(doc);
                    break;
                }
            }
        }

        if (clusterDocs.isEmpty()) {
            return 0.0;
        }

        double totalSilhouette = 0.0;

        for (Document doc : clusterDocs) {
            // Tính a(i) - khoảng cách trung bình trong cụm
            double a = 0.0;
            for (Document other : clusterDocs) {
                if (doc != other) {
                    double similarity = CosineSimilarity.calculate(
                            doc.getTfidfVector(),
                            other.getTfidfVector()
                    );
                    a += (1 - similarity); // Convert similarity to distance
                }
            }
            a /= Math.max(1, clusterDocs.size() - 1);

            // Tính b(i) - khoảng cách trung bình đến cụm gần nhất
            double b = Double.MAX_VALUE;
            for (Cluster otherCluster : clusters) {
                if (otherCluster.getId() != cluster.getId()) {
                    double similarity = CosineSimilarity.calculate(
                            doc.getTfidfVector(),
                            otherCluster.getCentroid()
                    );
                    double distance = 1 - similarity;
                    b = Math.min(b, distance);
                }
            }

            // Silhouette cho document này
            if (b != Double.MAX_VALUE) {
                double s = (b - a) / Math.max(a, b);
                totalSilhouette += s;
            }
        }

        return totalSilhouette / clusterDocs.size();
    }

    private Map<String, Double> getTopKeywordsForCluster(Cluster cluster, int topN) {
        Map<String, Double> keywordScores = new HashMap<>();

        // Cộng dồn TF-IDF scores của tất cả documents trong cluster
        for (int bookId : cluster.getBookIds()) {
            for (Document doc : processedDocuments) {
                if (doc.getBookId() == bookId) {
                    Map<String, Double> vector = doc.getTfidfVector();
                    for (Map.Entry<String, Double> entry : vector.entrySet()) {
                        keywordScores.merge(entry.getKey(), entry.getValue(), Double::sum);
                    }
                    break;
                }
            }
        }

        // Lấy top N từ khóa có score cao nhất
        return keywordScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private String formatKeywords(Map<String, Double> keywords) {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (Map.Entry<String, Double> entry : keywords.entrySet()) {
            if (count > 0) {
                sb.append(", ");
            }
            sb.append(String.format("%s (%.2f)", entry.getKey(), entry.getValue()));
            count++;

            // Xuống dòng sau mỗi 5 từ
            if (count % 5 == 0) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private void exportResults() {
        if (clusters == null || clusters.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa có kết quả phân cụm!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Xuất kết quả phân cụm");
        fileChooser.setSelectedFile(new File("clustering_results.txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file),
                            StandardCharsets.UTF_8))) {

                writer.println("=== KẾT QUẢ PHÂN CỤM K-MEANS ===");
                writer.println("Thời gian: " + new Date());
                writer.println("Số cụm: " + clusters.size());
                writer.println();

                List<Book> books = dbManager.getAllBooks();

                for (Cluster cluster : clusters) {
                    writer.println(String.format("CỤM %d (Số lượng: %d)",
                            cluster.getId() + 1, cluster.size()));
                    writer.println("─".repeat(80));

                    for (int bookId : cluster.getBookIds()) {
                        Book book = books.stream()
                                .filter(b -> b.getId() == bookId)
                                .findFirst()
                                .orElse(null);

                        if (book != null) {
                            writer.println(String.format("  - %s", book.getTitle()));
                            writer.println(String.format("    Tác giả: %s | Thể loại: %s | Năm: %d",
                                    book.getAuthor(), book.getCategory(), book.getYear()));
                            writer.println();
                        }
                    }
                    writer.println();
                }

                JOptionPane.showMessageDialog(this,
                        "Xuất kết quả thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xuất file: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveToDatabase() {
        if (clusters == null || clusters.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa có kết quả phân cụm!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn lưu kết quả phân cụm vào database?\n" +
                        "Thông tin cụm của các sách sẽ được cập nhật.",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int updated = 0;
                for (Cluster cluster : clusters) {
                    for (int bookId : cluster.getBookIds()) {
                        if (dbManager.updateBookCluster(bookId, cluster.getId())) {
                            updated++;
                        }
                    }
                }

                JOptionPane.showMessageDialog(this,
                        String.format("Đã cập nhật cụm cho %d sách!", updated),
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi lưu vào database: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void publish(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(message + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }
}
