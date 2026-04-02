package gui;

import gui.components.LeftSidebar;
import gui.panels.CandidatePanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class MainFrame extends JFrame {

	private LeftSidebar sidebar;
	private JPanel contentArea;
	private JLabel pageTitleLabel;

	public MainFrame() {
		setTitle("MHPL - Hệ Thống Xét Tuyển Đại Học");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1400, 800);
		setLocationRelativeTo(null);

		setupLayout();
		setVisible(true);
	}

	private void setupLayout() {
		// Create sidebar
		sidebar = new LeftSidebar(nodeName -> handleSidebarSelection(nodeName));

		// Create content area
		contentArea = new JPanel(new BorderLayout());
		contentArea.setBackground(Color.WHITE);
		contentArea.add(new CandidatePanel(), BorderLayout.CENTER);

		JPanel rightPanel = new JPanel(new BorderLayout(0, 12));
		rightPanel.setBackground(new Color(245, 246, 248));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		rightPanel.add(createTopHeader(), BorderLayout.NORTH);
		rightPanel.add(contentArea, BorderLayout.CENTER);

		// Create split pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, rightPanel);
		splitPane.setDividerLocation(250);
		splitPane.setDividerSize(1);

		setContentPane(splitPane);
		
		// Default to candidate list screen
		sidebar.selectItem(2);
	}

	private JPanel createTopHeader() {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(new Color(245, 246, 248));
		header.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(220, 223, 230), 1),
				BorderFactory.createEmptyBorder(14, 18, 14, 18)));

		pageTitleLabel = new JLabel("Danh sách thí sinh");
		pageTitleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		pageTitleLabel.setForeground(new Color(20, 30, 50));
		header.add(pageTitleLabel, BorderLayout.WEST);

		JPanel adminInfo = new JPanel();
		adminInfo.setOpaque(false);
		adminInfo.setLayout(new BoxLayout(adminInfo, BoxLayout.X_AXIS));

		JPanel adminText = new JPanel();
		adminText.setOpaque(false);
		adminText.setLayout(new BoxLayout(adminText, BoxLayout.Y_AXIS));

		JLabel adminTitle = new JLabel("Admin System");
		adminTitle.setFont(new Font("Arial", Font.BOLD, 18));
		adminTitle.setForeground(new Color(20, 30, 50));
		adminTitle.setAlignmentX(0.5f);

		JLabel adminRole = new JLabel("ADMIN");
		adminRole.setFont(new Font("Arial", Font.PLAIN, 13));
		adminRole.setForeground(new Color(120, 130, 150));
		adminRole.setAlignmentX(0.5f);

		adminText.add(adminTitle);
		adminText.add(Box.createVerticalStrut(2));
		adminText.add(adminRole);

		JPanel avatar = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(230, 236, 248));
				g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
				g2.setColor(new Color(190, 205, 235));
				g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
				g2.dispose();
			}
		};
		avatar.setOpaque(false);
		avatar.setPreferredSize(new Dimension(44, 44));
		avatar.setMinimumSize(new Dimension(44, 44));
		avatar.setMaximumSize(new Dimension(44, 44));
		avatar.setLayout(new BorderLayout());

		JLabel avatarText = new JLabel("AS", JLabel.CENTER);
		avatarText.setForeground(new Color(44, 97, 230));
		avatarText.setFont(new Font("Arial", Font.BOLD, 20));
		avatar.add(avatarText, BorderLayout.CENTER);

		adminInfo.add(adminText);
		adminInfo.add(Box.createHorizontalStrut(14));
		adminInfo.add(avatar);

		header.add(adminInfo, BorderLayout.EAST);
		return header;
	}

	private void handleSidebarSelection(String menuItem) {
		updatePageTitle(menuItem);

		switch (menuItem) {
			case "Dashboard":
				switchContent(new CandidatePanel());
				break;
			case "Quản trị User":
				showMessage("Quản trị User - Chưa Triển Khai");
				break;
			case "Quản lý Thí sinh":
				switchContent(new CandidatePanel());
				break;
			case "Cấu hình Tuyển sinh":
				showMessage("Cấu hình Tuyển sinh - Chưa Triển Khai");
				break;
			case "Quản lý Điểm":
				showMessage("Quản lý Điểm - Chưa Triển Khai");
				break;
			case "Nguyện vọng":
				showMessage("Nguyện vọng - Chưa Triển Khai");
				break;
			case "XÉT TUYỂN":
				showMessage("XÉT TUYỂN - Chưa Triển Khai");
				break;
			case "Thống kê":
				showMessage("Thống kê - Chưa Triển Khai");
				break;
			case "Hệ thống":
				showMessage("Hệ thống - Chưa Triển Khai");
				break;
			case "Đăng xuất":
				handleLogout();
				break;
		}
	}

	private void updatePageTitle(String menuItem) {
		if (pageTitleLabel == null) {
			return;
		}

		if ("Quản lý Thí sinh".equals(menuItem)) {
			pageTitleLabel.setText("Danh sách thí sinh");
			return;
		}

		if ("Dashboard".equals(menuItem)) {
			pageTitleLabel.setText("Tổng quan");
			return;
		}

		pageTitleLabel.setText(menuItem);
	}

	private void switchContent(JPanel panel) {
		contentArea.removeAll();
		contentArea.add(panel, BorderLayout.CENTER);
		contentArea.revalidate();
		contentArea.repaint();
	}

	private void handleLogout() {
		int confirm = JOptionPane.showConfirmDialog(this, 
			"Bạn có chắc muốn đăng xuất?", 
			"Xác Nhận", 
			JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}

	private void showMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
	}
}
