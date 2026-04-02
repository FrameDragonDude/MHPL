package gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class LeftSidebar extends JPanel {

	private JList<String> menuList;
	private Consumer<String> selectionCallback;
	private String[] menuItems = {
		"Dashboard",
		"Quản trị User",
		"Quản lý Thí sinh",
		"Cấu hình Tuyển sinh",
		"Quản lý Điểm",
		"Nguyện vọng",
		"XÉT TUYỂN",
		"Thống kê",
		"Hệ thống"
	};

	public LeftSidebar(Consumer<String> callback) {
		this.selectionCallback = callback;
		setLayout(new BorderLayout());
		setBackground(new Color(250, 250, 252));
		setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235), 1));

		// Header panel
		JPanel headerPanel = createHeaderPanel();
		add(headerPanel, BorderLayout.NORTH);

		// Menu list
		menuList = new JList<>(menuItems);
		menuList.setBackground(new Color(250, 250, 252));
		menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		menuList.setCellRenderer(new MenuCellRenderer());
		menuList.setFont(new Font("Arial", Font.PLAIN, 13));
		menuList.setFixedCellHeight(45);

		menuList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting() && selectionCallback != null && menuList.getSelectedValue() != null) {
				selectionCallback.accept(menuList.getSelectedValue());
			}
		});

		JScrollPane scrollPane = new JScrollPane(menuList);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setBackground(new Color(250, 250, 252));
		add(scrollPane, BorderLayout.CENTER);

		// Logout button
		JButton logoutBtn = new JButton("Đăng xuất");
		logoutBtn.setFont(new Font("Arial", Font.BOLD, 13));
		logoutBtn.setForeground(Color.WHITE);
		logoutBtn.setBackground(new Color(33, 150, 243));
		logoutBtn.setBorder(null);
		logoutBtn.setFocusPainted(false);
		logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		logoutBtn.addActionListener(e -> selectionCallback.accept("Đăng xuất"));

		JPanel footerPanel = new JPanel(new BorderLayout());
		footerPanel.setBackground(new Color(250, 250, 252));
		footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
		footerPanel.add(logoutBtn, BorderLayout.CENTER);

		add(footerPanel, BorderLayout.SOUTH);
	}

	private JPanel createHeaderPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(new Color(250, 250, 252));
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 235)),
			BorderFactory.createEmptyBorder(15, 15, 15, 15)
		));

		JLabel titleLabel = new JLabel("MHPL");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setForeground(new Color(33, 150, 243));

		JLabel subtitleLabel = new JLabel("Admin System");
		subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
		subtitleLabel.setForeground(new Color(128, 128, 128));

		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setBackground(new Color(250, 250, 252));
		textPanel.add(titleLabel);
		textPanel.add(Box.createVerticalStrut(3));
		textPanel.add(subtitleLabel);

		panel.add(textPanel, BorderLayout.WEST);

		return panel;
	}

	public String getSelectedItem() {
		return menuList.getSelectedValue();
	}

	public void selectItem(int index) {
		menuList.setSelectedIndex(index);
	}

	private class MenuCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
				boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (isSelected) {
				setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
				setBackground(new Color(33, 150, 243));
				setForeground(Color.WHITE);
				setFont(new Font("Arial", Font.BOLD, 13));
			} else {
				setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
				setBackground(new Color(250, 250, 252));
				setForeground(new Color(80, 80, 80));
				setFont(new Font("Arial", Font.PLAIN, 13));
			}

			return label;
		}
	}
}

