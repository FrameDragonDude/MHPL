package gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class AppHeader extends JPanel {

    public AppHeader() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 60));
        setBackground(new Color(37, 99, 235));
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        JLabel closeLabel = new JLabel("x");
        closeLabel.setForeground(Color.WHITE);
        closeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JLabel titleLabel = new JLabel("He thong quan ly tuyen sinh");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel subtitleLabel = new JLabel("Admin Dashboard");
        subtitleLabel.setForeground(new Color(219, 234, 254));
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(closeLabel, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        leftPanel.add(titlePanel, BorderLayout.CENTER);

        JLabel userLabel = new JLabel("Admin User");
        userLabel.setForeground(Color.WHITE);
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel emailLabel = new JLabel("admin@tuyensinh.edu.vn");
        emailLabel.setForeground(new Color(219, 234, 254));
        emailLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel rightPanel = new JPanel(new GridLayout(2, 1));
        rightPanel.setOpaque(false);
        rightPanel.add(userLabel);
        rightPanel.add(emailLabel);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }
}
