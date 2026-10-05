package gui;

import database.DatabaseManager;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private DatabaseManager dbManager;
    private JTabbedPane tabbedPane;
    private BookManagementPanel bookPanel;
    private ClusteringPanel clusteringPanel;

    public MainFrame() {
        dbManager = new DatabaseManager();
        initComponents();
    }

    private void initComponents() {
        setTitle("Hệ thống Quản lý Thư viện Số - Phân cụm Tài liệu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Tạo menu bar
        createMenuBar();

        // Tạo tabbed pane
        tabbedPane = new JTabbedPane();

        // Tab quản lý sách
        bookPanel = new BookManagementPanel(dbManager);
        tabbedPane.addTab("Quản lý Sách", new ImageIcon(), bookPanel, "Quản lý danh mục sách");

        // Tab phân cụm
        clusteringPanel = new ClusteringPanel(dbManager);
        tabbedPane.addTab("Phân cụm Tài liệu", new ImageIcon(), clusteringPanel, "Phân nhóm tài liệu tự động");

        add(tabbedPane);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Sẵn sàng");
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu File
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Thoát");
        exitItem.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn thoát?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dbManager.close();
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);

        // Menu Help
        JMenu helpMenu = new JMenu("Trợ giúp");
        JMenuItem aboutItem = new JMenuItem("Giới thiệu");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Hệ thống Quản lý Thư viện Số\n" +
                            "Sử dụng thuật toán K-Means Clustering\n" +
                            "với TF-IDF và Cosine Similarity\n\n" +
                            "Phiên bản 1.0 - 2025",
                    "Giới thiệu",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
