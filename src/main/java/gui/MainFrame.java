package gui;

import com.formdev.flatlaf.FlatClientProperties;
import dto.UserDTO;
import gui.components.AppHeader;
import gui.components.LeftSidebar;
import gui.panels.DashboardPanel;
import gui.panels.CandidatePanel;
import gui.panels.ChangePasswordPanel;
import gui.panels.NganhTuyenSinhPanel;
import gui.panels.MajorCombinationPanel;
import gui.panels.NguyenVongPanel;
import gui.panels.ConversionRulePanel;
import gui.panels.BonusPointPanel;
import gui.panels.SubjectCombinationPanel;
import gui.panels.UserManagementPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

    private final JPanel bodyPanel;
    private final LeftSidebar sidebar;
    private JPanel currentPanel;
    private boolean isSidebarVisible = true;
    private UserDTO user ;

    // TONE MÀU CHỦ ĐẠO MỚI
    private final Color SIDEBAR_PURPLE = new Color(87, 82, 174);
    private final Color MAIN_BG_BLUE = new Color(223, 234, 252); 
    private final Color HEADER_WHITE = Color.WHITE;

    public MainFrame() {
        setTitle("HỆ THỐNG QUẢN LÝ TUYỂN SINH - SGU 2026");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel headerContainer = createModernHeader();
        add(headerContainer, BorderLayout.NORTH);

        bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(MAIN_BG_BLUE);
        
        sidebar = new LeftSidebar(this);
        bodyPanel.add(sidebar, BorderLayout.WEST);

        // 3. Panel nội dung
        currentPanel = new DashboardPanel();
        bodyPanel.add(currentPanel, BorderLayout.CENTER);

        add(bodyPanel, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            sidebar.setActiveButton("DASHBOARD");
        });
    }

    private JPanel createModernHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(87, 82, 174));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 220, 240)));
        URL iconURL = getClass().getResource("/icons/icons8-bulleted-list-16.png");
        JButton btnToggle = new JButton("");
        if (iconURL != null) {
            ImageIcon toggleIcon = new ImageIcon(iconURL);
            btnToggle.setIcon(toggleIcon);
        } else {
            btnToggle.setText("☰"); 
            System.err.println("Không tìm thấy icon: icons8-bulleted-list-16.png");
        }
        btnToggle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnToggle.setPreferredSize(new Dimension(60, 60));
        btnToggle.setFocusPainted(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setContentAreaFilled(false);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggle.setForeground(SIDEBAR_PURPLE);
        
        btnToggle.putClientProperty(FlatClientProperties.STYLE, 
            "hoverBackground: #f1f5f9; " + 
            "pressedBackground: #e2e8f0");
        
        btnToggle.addActionListener(e -> toggleSidebar());

        JPanel appHeader = new AppHeader(this);
        appHeader.setOpaque(false);

        header.add(btnToggle, BorderLayout.WEST);
        header.add(appHeader, BorderLayout.CENTER);

        return header;
    }

    public void toggleSidebar() {
        isSidebarVisible = !isSidebarVisible;
        sidebar.setVisible(isSidebarVisible);
        revalidate();
        repaint();
    }

    public void switchPanel(String actionId) {
        JPanel newPanel = null;
        switch (actionId) {
            case "DASHBOARD": newPanel = new DashboardPanel(); break;
            case "CANDIDATE": newPanel = new CandidatePanel(); break;
            case "CATALOG": newPanel = new NganhTuyenSinhPanel(); break;
            case "SUBJECT": newPanel = new SubjectCombinationPanel(); break;
            case "MAJOR_SUBJECT": newPanel = new MajorCombinationPanel(); break;
            case "ASPIRATION": newPanel = new NguyenVongPanel(); break;
            case "BONUS_POINT": newPanel = new BonusPointPanel(); break;
            case "CONVERSION": newPanel = new ConversionRulePanel(); break;
            case "USER_MANAGEMENT": newPanel = new UserManagementPanel(); break;
            case "CHANGE_PASSWORD": newPanel = new ChangePasswordPanel(this); break;
            case "EXAM_SCORE":
            case "ADMISSION_RUN":
            case "AUDIT_LOG":
                showDevelopingMessage();
                return;
            default: return;
        }

        if (newPanel != null) {
            updateContentArea(newPanel, actionId);
        }
    }

    private void updateContentArea(JPanel newPanel, String actionId) {
        sidebar.setActiveButton(actionId);
        bodyPanel.remove(currentPanel);
        currentPanel = newPanel;
        bodyPanel.add(currentPanel, BorderLayout.CENTER);
        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    private void showDevelopingMessage() {
        JOptionPane.showMessageDialog(this,
                "Module đang được phát triển nâng cấp UI/UX.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public void logout() {
        int result = JOptionPane.showConfirmDialog(this,
                "Đăng xuất khỏi hệ thống?", "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            this.dispose();
            SwingUtilities.invokeLater(() -> { new LoginFrame().setVisible(true); });
        }
    }
}