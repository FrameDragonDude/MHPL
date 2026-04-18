package gui.components;

import com.formdev.flatlaf.FlatClientProperties;
import gui.SessionManager;
import gui.MainFrame;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AppHeader extends JPanel {

    private final MainFrame parentFrame;
    private final Color TEXT_MAIN = new Color(255,255,255);
    private final Color TEXT_SUB = new Color(199, 106,0); 

    public AppHeader(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(0, 20, 0, 20));

        add(createLeftTitle(), BorderLayout.WEST);

        add(createUserProfile(), BorderLayout.EAST);
    }

    private JPanel createLeftTitle() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("HỆ THỐNG TUYỂN SINH SGU");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_MAIN);

        JLabel subtitleLabel = new JLabel("Học kỳ 2 - Niên khóa 2025-2026");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_SUB);

        panel.add(titleLabel);
        panel.add(subtitleLabel);
        return panel;
    }

    private JPanel createUserProfile() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        panel.setOpaque(false);
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        String fullName = SessionManager.getCurrentUserFullname() != null ? 
                           SessionManager.getCurrentUserFullname() : "Admin";
        String role = SessionManager.isAdmin() ? "Quản trị viên" : "Nhân viên";

        JLabel nameLabel = new JLabel(fullName, SwingConstants.RIGHT);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_MAIN);

        JLabel roleLabel = new JLabel(role, SwingConstants.RIGHT);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(TEXT_MAIN); 

        textPanel.add(nameLabel);
        textPanel.add(roleLabel);


        JLabel avatarLabel = new JLabel();
        try {
            URL url = getClass().getResource("/icons/icons8-user-32.png"); 
            if (url != null) {
                avatarLabel.setIcon(new ImageIcon(url));
            }
        } catch (Exception ignored) {}
        
        panel.add(textPanel);
        panel.add(avatarLabel);

        JPopupMenu userMenu = createUserInfoPopup();
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                userMenu.show(panel, 0, panel.getHeight());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setOpaque(false);
                panel.repaint();
            }
        });

        return panel;
    }

    private JPopupMenu createUserInfoPopup() {
        JPopupMenu menu = new JPopupMenu();
        menu.putClientProperty(FlatClientProperties.STYLE, "arc: 12; border: 1,1,1,1,#e2e8f0");

        JMenuItem itemProfile = new JMenuItem(" Thông tin cá nhân");
        
        JMenuItem itemPassword = new JMenuItem(" Đổi mật khẩu");
        itemPassword.addActionListener(e -> {
            if (parentFrame != null) {
                parentFrame.switchPanel("CHANGE_PASSWORD");
            }
        });

        JMenuItem itemLogout = new JMenuItem(" Đăng xuất");
        itemLogout.setForeground(Color.RED);
        itemLogout.addActionListener(e -> {
            if (parentFrame != null) parentFrame.logout();
        });

        menu.add(itemProfile);
        menu.add(itemPassword);
        menu.addSeparator();
        menu.add(itemLogout);

        return menu;
    }
}