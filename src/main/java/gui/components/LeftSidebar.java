package gui.components;

import gui.SessionManager;
import gui.MainFrame;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LeftSidebar extends JPanel {

    private final MainFrame parentFrame;
    private final Map<String, JButton> menuButtons = new HashMap<>();
    private String currentActiveId = "DASHBOARD";

    private final Color SIDEBAR_COLOR = new Color(87, 82, 174);
    private final Color ACTIVE_BG = new Color(107, 102, 194);
    private final Color TEXT_WHITE = Color.WHITE;

    public LeftSidebar(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(SIDEBAR_COLOR);
        setPreferredSize(new Dimension(260, 0));
        setBorder(null);

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(new EmptyBorder(20, 15, 20, 15));

        initMenu(menuPanel);
        add(menuPanel, BorderLayout.CENTER);
        setActiveButton(currentActiveId);
    }

    private void initMenu(JPanel panel) {
        panel.add(createMenuButton("Dashboard", "icons8-dashboard-16.png", "DASHBOARD"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createMenuButton("Thí sinh", "icons8-candidate-16.png", "CANDIDATE"));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createMenuButton("Ngành tuyển sinh", "icons8-catalog-16.png", "CATALOG"));
        panel.add(createMenuButton("Tổ hợp môn", "icons8-elective-16.png", "SUBJECT"));
        panel.add(createMenuButton("Ngành - Tổ hợp", "icons8-biology-book-16.png", "MAJOR_SUBJECT"));
        panel.add(createMenuButton("Điểm thí sinh", "icons8-ball-point-pen-16.png", "EXAM_SCORE"));
        panel.add(createMenuButton("Điểm cộng", "icons8-grades-16.png", "BONUS_POINT"));
        panel.add(createMenuButton("Ưu tiên xét tuyển", "icons8-admission-16.png", "PRIORITY_ADMISSION"));
        panel.add(createMenuButton("Nguyện vọng", "icons8-employee-16.png", "ASPIRATION"));
        panel.add(createMenuButton("Bảng quy đổi", "icons8-conversion-16.png", "CONVERSION"));

        if (SessionManager.isAdmin()) {
            panel.add(Box.createVerticalStrut(15));
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(255, 255, 255, 50));
            panel.add(sep);
            panel.add(Box.createVerticalStrut(15));
            panel.add(createMenuButton("Quản lý tài khoản", "icons8-user-16.png", "USER_MANAGEMENT"));
            panel.add(createMenuButton("Chạy tuyên dương", "icons8-performance-goal-16.png", "ADMISSION_RUN"));
            panel.add(createMenuButton("Audit Log", "icons8-audit-16.png", "AUDIT_LOG"));
        }

        panel.add(Box.createVerticalGlue());
        panel.add(createLogoutButton());
    }

    private JPanel createMenuButton(String title, String iconName, String actionId) {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton button = new JButton(title);
        try {
            URL res = getClass().getResource("/icons/" + iconName);
            if (res != null) { button.setIcon(new ImageIcon(res)); button.setIconTextGap(12); }
        } catch (Exception e) {}

        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(TEXT_WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setPreferredSize(new Dimension(230, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {
            setActiveButton(actionId);
            if (parentFrame != null) parentFrame.switchPanel(actionId);
        });

        menuButtons.put(actionId, button);
        wrap.add(button);
        return wrap;
    }

    public void setActiveButton(String actionId) {
        menuButtons.forEach((id, btn) -> {
            btn.setOpaque(false);
            btn.setBackground(null);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        });

        JButton activeBtn = menuButtons.get(actionId);
        if (activeBtn != null) {
            activeBtn.setOpaque(true);
            activeBtn.setBackground(ACTIVE_BG);
            activeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            currentActiveId = actionId;
        }
    }

    private JPanel createLogoutButton() {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        JButton logoutBtn = new JButton("Đăng xuất");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setForeground(TEXT_WHITE);
        logoutBtn.setBackground(new Color(255, 255, 255, 30));
        logoutBtn.setOpaque(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setPreferredSize(new Dimension(230, 40));
        logoutBtn.addActionListener(e -> { if (parentFrame != null) parentFrame.logout(); });
        wrap.add(logoutBtn);
        return wrap;
    }
}