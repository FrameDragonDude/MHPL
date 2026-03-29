package gui;

import gui.panels.CandidatePanel;

import javax.swing.JFrame;

public class MainFrame extends JFrame {

	public MainFrame() {
		setTitle("MHPL - Candidate Management");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1200, 700);
		setLocationRelativeTo(null);

		setContentPane(new CandidatePanel());
	}
}
