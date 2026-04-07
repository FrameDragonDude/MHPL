package gui;

import gui.components.AppHeader;
import gui.components.LeftSidebar;
import gui.panels.CandidatePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame {

	public MainFrame() {
		setTitle("HỆ THỐNG QUẢN LÝ TUYỂN SINH");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1400, 800);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		add(new AppHeader(), BorderLayout.NORTH);

		JPanel bodyPanel = new JPanel(new BorderLayout());
		bodyPanel.setBackground(new Color(241, 245, 249));
		bodyPanel.add(new LeftSidebar(), BorderLayout.WEST);
		bodyPanel.add(new CandidatePanel(), BorderLayout.CENTER);

		add(bodyPanel, BorderLayout.CENTER);
	}
}
