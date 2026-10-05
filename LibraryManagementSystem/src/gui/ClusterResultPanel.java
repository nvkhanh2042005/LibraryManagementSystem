package gui;

import database.DatabaseManager;
import models.Book;
import models.Cluster;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ClusterResultPanel extends JPanel {
    private DatabaseManager dbManager;
    private List<Cluster> clusters;
    private JList<String> clusterList;
    private DefaultListModel<String> listModel;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextArea txtStatistics;
    private JLabel lblClusterInfo;
    private Map<Integer, Color> clusterColors;

    public ClusterResultPanel(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.clusterColors = new HashMap<>();
        initializeColors();
        initComponents();
    }

    private void initializeColors() {
        // Bảng màu cho các cụm
        clusterColors.put(0, new Color(255, 182, 193)); // Light Pink
        clusterColors.put(1, new Color(173, 216, 230)); // Light Blue
        clusterColors.put(2, new Color(144, 238, 144)); // Light Green
        clusterColors.put(3, new Color(255, 218, 185)); // Peach
        clusterColors.put(4, new Color(221, 160, 221)); // Plum
        clusterColors.put(5, new Color(255, 255, 153)); // Light Yellow
        clusterColors.put(6, new Color(255, 204, 153)); // Light Orange
        clusterColors.put(7, new Color(204, 229, 255)); // Light Sky Blue
        clusterColors.put(8, new Color(204, 255, 204)); // Mint Green
        clusterColors.put(9, new Color(255, 204, 229)); // Light Rose
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Left panel - Cluster list
        JPanel leftPanel = createClusterListPanel();

        // Center panel - Book details
        JPanel centerPanel = createBookDetailsPanel();

        // Right panel - Statistics
        JPanel rightPanel = createStatisticsPanel();

        // Split pane
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setResizeWeight(0.2);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplit.setLeftComponent(centerPanel);
        rightSplit.setRightComponent(rightPanel);
        rightSplit.setResizeWeight(0.6);

        mainSplit.setRightComponent(rightSplit);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createClusterListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Danh sách Cụm"));
        panel.setPreferredSize(new Dimension(200, 0));

        listModel = new DefaultListModel<>();
        clusterList = new JList<>(listModel);
        clusterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clusterList.setCellRenderer(new ClusterListRenderer());
        clusterList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadClusterBooks();
            }
        });

        JScrollPane scrollPane = new JScrollPane(clusterList);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBookDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Chi tiết Tài liệu"));

        // Info label
        lblClusterInfo = new JLabel("Chọn một cụm để xem chi tiết");
        lblClusterInfo.setFont(new Font("Arial", Font.BOLD, 12));
        lblClusterInfo.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(lblClusterInfo, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Tên sách", "Tác giả", "Thể loại", "Năm"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setRowHeight(25);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer cho bảng
        bookTable.setDefaultRenderer(Object.class, new BookTableRenderer());

        JScrollPane scrollPane = new JScrollPane(bookTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnViewDetails = new JButton("Xem chi tiết");
        btnViewDetails.addActionListener(e -> viewBookDetails());
        buttonPanel.add(btnViewDetails);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Thống kê"));
        panel.setPreferredSize(new Dimension(250, 0));

        txtStatistics = new JTextArea();
        txtStatistics.setEditable(false);
        txtStatistics.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtStatistics.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(txtStatistics);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void displayClusters(List<Cluster> clusters) {
        this.clusters = clusters;
        listModel.clear();

        if (clusters == null || clusters.isEmpty()) {
            listModel.addElement("Chưa có dữ liệu phân cụm");
            return;
        }

        for (Cluster cluster : clusters) {
            String item = String.format("Cụm %d (%d)",
                    cluster.getId() + 1, cluster.size());
            listModel.addElement(item);
        }

        updateOverallStatistics();

        // Auto select first cluster
        if (!clusters.isEmpty()) {
            clusterList.setSelectedIndex(0);
        }
    }

    private void loadClusterBooks() {
        int selectedIndex = clusterList.getSelectedIndex();
        if (selectedIndex < 0 || clusters == null || selectedIndex >= clusters.size()) {
            return;
        }

        Cluster cluster = clusters.get(selectedIndex);
        tableModel.setRowCount(0);

        lblClusterInfo.setText(String.format("Cụm %d - %d tài liệu",
                cluster.getId() + 1, cluster.size()));

        List<Book> allBooks = dbManager.getAllBooks();
        int count = 0;

        for (int bookId : cluster.getBookIds()) {
            Book book = allBooks.stream()
                    .filter(b -> b.getId() == bookId)
                    .findFirst()
                    .orElse(null);

            if (book != null) {
                Object[] row = {
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getCategory(),
                        book.getYear()
                };
                tableModel.addRow(row);
                count++;
            }
        }

        updateClusterStatistics(cluster, count);
    }

    private void updateOverallStatistics() {
        if (clusters == null || clusters.isEmpty()) {
            txtStatistics.setText("Chưa có dữ liệu thống kê");
            return;
        }

        StringBuilder stats = new StringBuilder();
        stats.append("=== TỔNG QUAN ===\n\n");
        stats.append(String.format("Tổng số cụm: %d\n", clusters.size()));

        int totalBooks = 0;
        int minSize = Integer.MAX_VALUE;
        int maxSize = 0;

        for (Cluster cluster : clusters) {
            int size = cluster.size();
            totalBooks += size;
            minSize = Math.min(minSize, size);
            maxSize = Math.max(maxSize, size);
        }

        stats.append(String.format("Tổng tài liệu: %d\n\n", totalBooks));
        stats.append(String.format("Cụm lớn nhất: %d tài liệu\n", maxSize));
        stats.append(String.format("Cụm nhỏ nhất: %d tài liệu\n", minSize));

        double avgSize = totalBooks / (double) clusters.size();
        stats.append(String.format("Trung bình: %.1f tài liệu/cụm\n\n", avgSize));

        stats.append("=== PHÂN BỐ ===\n\n");
        for (Cluster cluster : clusters) {
            int size = cluster.size();
            double percentage = (size * 100.0) / totalBooks;
            stats.append(String.format("Cụm %d: %d (%.1f%%)\n",
                    cluster.getId() + 1, size, percentage));

            // Progress bar visualization
            int barLength = (int) (percentage / 5);
            stats.append("  [");
            for (int i = 0; i < 20; i++) {
                stats.append(i < barLength ? "█" : "·");
            }
            stats.append("]\n\n");
        }

        txtStatistics.setText(stats.toString());
    }

    private void updateClusterStatistics(Cluster cluster, int bookCount) {
        StringBuilder stats = new StringBuilder();
        stats.append(String.format("=== CỤM %d ===\n\n", cluster.getId() + 1));
        stats.append(String.format("Số tài liệu: %d\n\n", bookCount));

        // Thống kê theo thể loại
        Map<String, Integer> categoryCount = new HashMap<>();
        List<Book> allBooks = dbManager.getAllBooks();

        for (int bookId : cluster.getBookIds()) {
            Book book = allBooks.stream()
                    .filter(b -> b.getId() == bookId)
                    .findFirst()
                    .orElse(null);

            if (book != null && book.getCategory() != null) {
                String category = book.getCategory();
                categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
            }
        }

        if (!categoryCount.isEmpty()) {
            stats.append("=== THỂ LOẠI ===\n\n");
            categoryCount.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .forEach(entry -> {
                        stats.append(String.format("• %s: %d\n", entry.getKey(), entry.getValue()));
                    });
        }

        txtStatistics.setText(stats.toString());
    }

    private void viewBookDetails() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một tài liệu!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int bookId = (int) tableModel.getValueAt(selectedRow, 0);
        Book book = dbManager.getBookById(bookId);

        if (book != null) {
            showBookDetailsDialog(book);
        }
    }

    private void showBookDetailsDialog(Book book) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Chi tiết Tài liệu", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Title
        JLabel lblTitle = new JLabel(book.getTitle());
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(10));

        // Info
        addInfoRow(contentPanel, "Tác giả:", book.getAuthor());
        addInfoRow(contentPanel, "Thể loại:", book.getCategory());
        addInfoRow(contentPanel, "Năm xuất bản:", String.valueOf(book.getYear()));
        addInfoRow(contentPanel, "Cụm:", book.getClusterId() >= 0 ?
                "Cụm " + (book.getClusterId() + 1) : "Chưa phân cụm");

        contentPanel.add(Box.createVerticalStrut(10));

        // Description
        JLabel lblDescTitle = new JLabel("Mô tả:");
        lblDescTitle.setFont(new Font("Arial", Font.BOLD, 12));
        lblDescTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblDescTitle);
        contentPanel.add(Box.createVerticalStrut(5));

        JTextArea txtDescription = new JTextArea(book.getDescription());
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setEditable(false);
        txtDescription.setRows(3);
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descScroll);

        contentPanel.add(Box.createVerticalStrut(10));

        // Content
        JLabel lblContentTitle = new JLabel("Nội dung:");
        lblContentTitle.setFont(new Font("Arial", Font.BOLD, 12));
        lblContentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblContentTitle);
        contentPanel.add(Box.createVerticalStrut(5));

        JTextArea txtContent = new JTextArea(book.getContent());
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setEditable(false);
        txtContent.setRows(8);
        JScrollPane contentScroll = new JScrollPane(txtContent);
        contentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(contentScroll);

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        dialog.add(mainScroll, BorderLayout.CENTER);

        // Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        buttonPanel.add(btnClose);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.BOLD, 12));
        lblLabel.setPreferredSize(new Dimension(120, 20));
        rowPanel.add(lblLabel);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 12));
        rowPanel.add(lblValue);

        panel.add(rowPanel);
    }

    // Custom renderer cho cluster list
    private class ClusterListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (clusters != null && index < clusters.size()) {
                Color color = clusterColors.getOrDefault(index, Color.LIGHT_GRAY);
                if (!isSelected) {
                    c.setBackground(color);
                    c.setForeground(Color.BLACK);
                }
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 5, 1, 0, color.darker()),
                        BorderFactory.createEmptyBorder(5, 10, 5, 5)
                ));
            }

            return c;
        }
    }

    // Custom renderer cho book table
    private class BookTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected && clusters != null) {
                int selectedClusterIndex = clusterList.getSelectedIndex();
                if (selectedClusterIndex >= 0 && selectedClusterIndex < clusters.size()) {
                    Color color = clusterColors.getOrDefault(selectedClusterIndex, Color.WHITE);
                    c.setBackground(color);
                } else {
                    c.setBackground(Color.WHITE);
                }
            }

            return c;
        }
    }
}
