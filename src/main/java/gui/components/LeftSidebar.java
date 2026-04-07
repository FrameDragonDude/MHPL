package gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class LeftSidebar extends JPanel {

	public LeftSidebar() {
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(220, 0));
		setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(226, 232, 240)));

		JPanel menuPanel = new JPanel();
		menuPanel.setBackground(Color.WHITE);
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
		menuPanel.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));

		menuPanel.add(createMenuButton("Nguoi dung", false));
		menuPanel.add(createMenuButton("Thi sinh", true));
		menuPanel.add(createMenuButton("Nganh tuyen sinh", false));
		menuPanel.add(createMenuButton("To hop mon", false));
		menuPanel.add(createMenuButton("Nganh - To hop", false));
		menuPanel.add(createMenuButton("Diem thi sinh", false));
		menuPanel.add(createMenuButton("Diem cong", false));
		menuPanel.add(createMenuButton("Nguyen vong", false));
		menuPanel.add(createMenuButton("Bang quy doi", false));
		menuPanel.add(Box.createVerticalGlue());

		add(menuPanel, BorderLayout.CENTER);
	}

	private JPanel createMenuButton(String title, boolean active) {
		JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		wrap.setOpaque(false);
		wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
		wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

		JButton button = new JButton("  " + title);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setOpaque(true);
		button.setHorizontalAlignment(JButton.LEFT);
		button.setPreferredSize(new Dimension(190, 34));
		button.setFont(new Font("Segoe UI", Font.PLAIN, 14));

		if (active) {
			button.setBackground(new Color(239, 246, 255));
			button.setForeground(new Color(37, 99, 235));
		} else {
			button.setBackground(Color.WHITE);
			button.setForeground(new Color(51, 65, 85));
		}

		wrap.add(button);
		return wrap;
	}
}