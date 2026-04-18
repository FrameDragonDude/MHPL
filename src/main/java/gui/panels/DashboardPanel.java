package gui.panels;

import bus.CandidateService;
import gui.SessionManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class DashboardPanel extends JPanel {

    private final CandidateService candidateService;
    private final Color MAIN_BG = new Color(223, 234, 252);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private final Color PURPLE_ACCENT = new Color(87, 82, 174);

    public DashboardPanel() {
        this.candidateService = new CandidateService();
        setupPanel();
    }

    private void setupPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 0, 10, 0);

        // Thống kê nhanh
        JPanel statsContainer = new JPanel(new GridLayout(1, 4, 25, 0));
        statsContainer.setOpaque(false);
        statsContainer.add(createStatCard("Thí sinh", getCandidateCount(), "icons8-student-16.png", new Color(230, 240, 255)));
        statsContainer.add(createStatCard("Ngành học", "05", "icons8-catalog-16.png", new Color(240, 255, 240)));
        statsContainer.add(createStatCard("Tổ hợp môn", "04", "icons8-list-16.png", new Color(255, 250, 240)));
        statsContainer.add(createStatCard("Người dùng", "03", "icons8-user-16.png", new Color(255, 240, 240)));

        gbc.gridy = 0; gbc.weighty = 0.2;
        mainContent.add(statsContainer, gbc);

        // Chi tiết
        JPanel detailsContainer = new JPanel(new GridLayout(1, 2, 25, 0));
        detailsContainer.setOpaque(false);
        detailsContainer.add(createDetailCard("Thông tin tài khoản", createUserInfoContent()));
        detailsContainer.add(createDetailCard("Hệ thống", createSystemInfoContent()));

        gbc.gridy = 1; gbc.weighty = 0.8;
        mainContent.add(detailsContainer, gbc);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Chào mừng trở lại!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Tổng quan hệ thống ngày " + java.time.LocalDate.now().toString());
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_SECONDARY);

        Box v = Box.createVerticalBox();
        v.add(title); v.add(sub);
        panel.add(v, BorderLayout.WEST);
        return panel;
    }

    private JPanel createStatCard(String title, String val, String icon, Color iconBg) {
        JPanel card = new RoundedPanel(20, CARD_COLOR);
        card.setLayout(new BorderLayout(15, 0));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel iconLb = new JLabel();
        try { iconLb.setIcon(new ImageIcon(getClass().getResource("/icons/" + icon))); } catch(Exception e){}
        iconLb.setOpaque(true);
        iconLb.setBackground(iconBg);
        iconLb.setPreferredSize(new Dimension(50, 50));
        iconLb.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 5));
        info.setOpaque(false);
        JLabel v = new JLabel(val); v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT_SECONDARY);
        info.add(v); info.add(t);

        card.add(iconLb, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDetailCard(String title, JPanel content) {
        JPanel card = new RoundedPanel(20, CARD_COLOR);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 18));
        t.setBorder(new EmptyBorder(0, 0, 20, 0));
        card.add(t, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createUserInfoContent() {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 10)); p.setOpaque(false);
        p.add(new JLabel("Họ tên: " + SessionManager.getCurrentUserFullname()));
        p.add(new JLabel("Tên đăng nhập: " + SessionManager.getCurrentUsername()));
        p.add(new JLabel("Quyền: " + (SessionManager.isAdmin() ? "Quản trị" : "Nhân viên")));
        return p;
    }

    private JPanel createSystemInfoContent() {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 10)); p.setOpaque(false);
        p.add(new JLabel("Java Version: " + System.getProperty("java.version")));
        p.add(new JLabel("OS: " + System.getProperty("os.name")));
        p.add(new JLabel("DB: MySQL 8.0"));
        return p;
    }

    private String getCandidateCount() {
        try { return String.format("%02d", candidateService.countRows("", "")); }
        catch (Exception e) { return "00"; }
    }

    class RoundedPanel extends JPanel {
        private int r; Color bg;
        RoundedPanel(int r, Color bg) { this.r = r; this.bg = bg; setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), r, r));
            g2.setColor(new Color(0,0,0,20)); // Viền nhẹ giả bóng
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, r, r));
            g2.dispose();
        }
    }
}