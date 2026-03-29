package gui.panels;

import bus.CandidateService;
import dto.CandidateDTO;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

public class CandidatePanel extends JPanel {

	private final CandidateService candidateService;
	private final DefaultTableModel tableModel;
	private final JTable table;

	private final JTextField txtSearchCccd;
	private final JTextField txtSearchName;
	private final JTextField txtHo;
	private final JTextField txtTen;
	private final JTextField txtNgaySinh;
	private final JTextField txtDienThoai;
	private final JTextField txtGioiTinh;
	private final JTextField txtEmail;
	private final JTextField txtNoiSinh;
	private final JTextField txtDoiTuong;
	private final JTextField txtKhuVuc;

	private final JLabel lblPaging;
	private final JLabel lblRows;

	private int currentPage = 1;
	private int totalPages = 1;

	public CandidatePanel() {
		this.candidateService = new CandidateService();
		this.tableModel = new DefaultTableModel(new String[]{
				"ID", "CCCD", "So bao danh", "Ho", "Ten", "Ngay sinh", "Dien thoai", "Email"
		}, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		this.table = new JTable(tableModel);

		this.txtSearchCccd = new JTextField(14);
		this.txtSearchName = new JTextField(18);
		this.txtHo = new JTextField(12);
		this.txtTen = new JTextField(12);
		this.txtNgaySinh = new JTextField(10);
		this.txtDienThoai = new JTextField(10);
		this.txtGioiTinh = new JTextField(8);
		this.txtEmail = new JTextField(16);
		this.txtNoiSinh = new JTextField(12);
		this.txtDoiTuong = new JTextField(10);
		this.txtKhuVuc = new JTextField(8);

		this.lblPaging = new JLabel("Page 1/1 (20 rows/page)");
		this.lblRows = new JLabel("Total rows: 0");

		setLayout(new BorderLayout(8, 8));
		add(buildSearchPanel(), BorderLayout.NORTH);
		add(buildTablePanel(), BorderLayout.CENTER);
		add(buildBottomPanel(), BorderLayout.SOUTH);

		loadPage(1);
	}

	private JPanel buildSearchPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton btnSearch = new JButton("Search");
		JButton btnReset = new JButton("Reset");

		panel.add(new JLabel("CCCD:"));
		panel.add(txtSearchCccd);
		panel.add(new JLabel("Ho ten:"));
		panel.add(txtSearchName);
		panel.add(btnSearch);
		panel.add(btnReset);

		btnSearch.addActionListener(e -> loadPage(1));
		btnReset.addActionListener(e -> {
			txtSearchCccd.setText("");
			txtSearchName.setText("");
			loadPage(1);
		});

		return panel;
	}

	private JPanel buildTablePanel() {
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		applyFixedColumnWidths();
		table.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				bindSelectedRowToForm();
			}
		});
		return new JPanel(new BorderLayout()) {{
			add(new JScrollPane(table), BorderLayout.CENTER);
		}};
	}

	private void applyFixedColumnWidths() {
		int[] widths = {70, 150, 130, 130, 110, 110, 130, 220};
		for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMinWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMaxWidth(widths[i]);
		}
	}

	private JPanel buildBottomPanel() {
		JPanel wrapper = new JPanel(new BorderLayout(8, 8));

		JPanel form = new JPanel(new GridLayout(3, 8, 6, 6));
		form.add(new JLabel("Ho"));
		form.add(txtHo);
		form.add(new JLabel("Ten"));
		form.add(txtTen);
		form.add(new JLabel("Ngay sinh"));
		form.add(txtNgaySinh);
		form.add(new JLabel("Dien thoai"));
		form.add(txtDienThoai);

		form.add(new JLabel("Gioi tinh"));
		form.add(txtGioiTinh);
		form.add(new JLabel("Email"));
		form.add(txtEmail);
		form.add(new JLabel("Noi sinh"));
		form.add(txtNoiSinh);
		form.add(new JLabel("Doi tuong"));
		form.add(txtDoiTuong);

		form.add(new JLabel("Khu vuc"));
		form.add(txtKhuVuc);
		form.add(new JLabel());
		form.add(new JLabel());
		form.add(new JLabel());
		form.add(new JLabel());
		form.add(new JLabel());
		form.add(new JLabel());

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnPrev = new JButton("Prev");
		JButton btnNext = new JButton("Next");
		JButton btnSave = new JButton("Save selected");
		JButton btnReload = new JButton("Reload");

		controls.add(lblRows);
		controls.add(lblPaging);
		controls.add(btnPrev);
		controls.add(btnNext);
		controls.add(btnSave);
		controls.add(btnReload);

		btnPrev.addActionListener(e -> {
			if (currentPage > 1) {
				loadPage(currentPage - 1);
			}
		});

		btnNext.addActionListener(e -> {
			if (currentPage < totalPages) {
				loadPage(currentPage + 1);
			}
		});

		btnSave.addActionListener(e -> saveSelectedCandidate());
		btnReload.addActionListener(e -> loadPage(currentPage));

		wrapper.add(form, BorderLayout.CENTER);
		wrapper.add(controls, BorderLayout.SOUTH);
		return wrapper;
	}

	private void loadPage(int page) {
		try {
			String cccd = txtSearchCccd.getText();
			String name = txtSearchName.getText();

			int countedPages = candidateService.countPages(cccd, name);
			int safePage = Math.max(1, Math.min(page, countedPages));

			List<CandidateDTO> rows = candidateService.getCandidates(cccd, name, safePage);
			int totalRows = candidateService.countRows(cccd, name);

			tableModel.setRowCount(0);
			for (CandidateDTO c : rows) {
				tableModel.addRow(new Object[]{
						c.getIdThisinh(),
						c.getCccd(),
						c.getSoBaoDanh(),
						c.getHo(),
						c.getTen(),
						c.getNgaySinh(),
						c.getDienThoai(),
						c.getEmail()
				});
			}

			this.currentPage = safePage;
			this.totalPages = countedPages;
			this.lblPaging.setText("Page " + currentPage + "/" + totalPages + " (20 rows/page)");
			this.lblRows.setText("Total rows: " + totalRows);
			clearForm();
		} catch (SQLException ex) {
			showError("Can not load candidate list", ex);
		}
	}

	private void bindSelectedRowToForm() {
		int row = table.getSelectedRow();
		if (row < 0) {
			clearForm();
			return;
		}
		txtHo.setText(stringValueAt(row, 3));
		txtTen.setText(stringValueAt(row, 4));
		txtNgaySinh.setText(stringValueAt(row, 5));
		txtDienThoai.setText(stringValueAt(row, 6));
		txtEmail.setText(stringValueAt(row, 7));
	}

	private void saveSelectedCandidate() {
		int row = table.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "Please select one row first.", "Info", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		CandidateDTO candidate = new CandidateDTO();
		candidate.setIdThisinh((Integer) tableModel.getValueAt(row, 0));
		candidate.setCccd(stringValueAt(row, 1));
		candidate.setSoBaoDanh(stringValueAt(row, 2));
		candidate.setHo(txtHo.getText());
		candidate.setTen(txtTen.getText());
		candidate.setNgaySinh(txtNgaySinh.getText());
		candidate.setDienThoai(txtDienThoai.getText());
		candidate.setGioiTinh(txtGioiTinh.getText());
		candidate.setEmail(txtEmail.getText());
		candidate.setNoiSinh(txtNoiSinh.getText());
		candidate.setDoiTuong(txtDoiTuong.getText());
		candidate.setKhuVuc(txtKhuVuc.getText());

		try {
			boolean updated = candidateService.updateCandidate(candidate);
			if (updated) {
				JOptionPane.showMessageDialog(this, "Updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
				loadPage(currentPage);
			} else {
				JOptionPane.showMessageDialog(this, "Update failed. Check required fields.", "Warning", JOptionPane.WARNING_MESSAGE);
			}
		} catch (SQLException ex) {
			showError("Can not update candidate", ex);
		}
	}

	private String stringValueAt(int row, int col) {
		Object value = tableModel.getValueAt(row, col);
		return value == null ? "" : value.toString();
	}

	private void clearForm() {
		txtHo.setText("");
		txtTen.setText("");
		txtNgaySinh.setText("");
		txtDienThoai.setText("");
		txtGioiTinh.setText("");
		txtEmail.setText("");
		txtNoiSinh.setText("");
		txtDoiTuong.setText("");
		txtKhuVuc.setText("");
	}

	private void showError(String title, Exception ex) {
		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
				this,
				title + ": " + ex.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE
		));
	}
}
