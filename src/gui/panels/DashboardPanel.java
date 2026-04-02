package gui.panels;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

	public DashboardPanel() {
		setLayout(new BorderLayout(20, 20));
		setBackground(new Color(245, 246, 248));
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// Title
		JLabel titleLabel = new JLabel("Tổng quan");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
		titleLabel.setForeground(new Color(33, 33, 33));
		add(titleLabel, BorderLayout.NORTH);

		// Stats cards panel
		JPanel statsPanel = createStatsPanel();
		add(statsPanel, BorderLayout.CENTER);
	}

	private JPanel createStatsPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
		panel.setOpaque(false);

		// Doanh thu hôm nay
		panel.add(createStatCard("Doanh thu hôm nay", "0 đ", new Color(76, 175, 80)));

		// Đơn hàng hôm nay
		panel.add(createStatCard("Đơn hàng hôm nay", "0", new Color(33, 150, 243)));

		// Tổng sản phẩm
		panel.add(createStatCard("Tổng sản phẩm", "0", new Color(156, 39, 176)));

		// Khách hàng
		panel.add(createStatCard("Khách hàng", "0", new Color(255, 152, 0)));

		return panel;
	}

	private JPanel createStatCard(String title, String value, Color color) {
		JPanel card = new JPanel(new BorderLayout(15, 10));
		card.setBackground(Color.WHITE);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
			BorderFactory.createEmptyBorder(20, 20, 20, 20)
		));

		// Icon circle
		JPanel iconPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(color);
				g2d.fillOval(0, 0, 60, 60);
				g2d.setColor(Color.WHITE);
				g2d.setFont(new Font("Arial", Font.BOLD, 24));
				FontMetrics fm = g2d.getFontMetrics();
				String icon = "📊";
				g2d.drawString(icon, (60 - fm.stringWidth(icon)) / 2, 20 + fm.getAscent());
			}
		};
		iconPanel.setPreferredSize(new Dimension(60, 60));
		iconPanel.setOpaque(false);

		// Text panel
		JPanel textPanel = new JPanel(new BorderLayout(0, 5));
		textPanel.setOpaque(false);

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
		titleLabel.setForeground(new Color(128, 128, 128));

		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
		valueLabel.setForeground(new Color(33, 33, 33));

		textPanel.add(titleLabel, BorderLayout.NORTH);
		textPanel.add(valueLabel, BorderLayout.CENTER);

		card.add(iconPanel, BorderLayout.WEST);
		card.add(textPanel, BorderLayout.CENTER);

		return card;
	}
}
