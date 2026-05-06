package gui.panels;

import bus.CandidateService;
import dto.CandidateDTO;
import gui.dialogs.CandidateFormDialog;
import utils.excel.CandidateExcelExportUtil;
import utils.excel.CandidateExcelImportUtil;

import javax.swing.JButton;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CandidatePanel extends JPanel {
	private static final Color COLOR_GREEN = new Color(46, 125, 50);
	private static final Color COLOR_BLUE = new Color(21, 101, 192);
	private static final Color COLOR_BLUE_SOFT = new Color(30, 136, 229);
	private static final Color COLOR_RED = new Color(198, 40, 40);
	private static final int PAGE_SIZE = 20;

	private static final String[] TABLE_COLUMNS = {
			"STT", "CCCD", "Số báo danh", "Họ Tên", "Ngày sinh", "Điện thoại", "Giới tính", "Email", "Nơi sinh", "Đối tượng", "Khu vực", "Chương trình", "Dân tộc", "Mã dân tộc", "Thao tác"
	};

	private static final int COL_STT = 0;
	private static final int COL_CCCD = 1;
	private static final int COL_SO_BAO_DANH = 2;
	private static final int COL_HO_TEN = 3;
	private static final int COL_NGAY_SINH = 4;
	private static final int COL_DIEN_THOAI = 5;
	private static final int COL_GIOI_TINH = 6;
	private static final int COL_EMAIL = 7;
	private static final int COL_NOI_SINH = 8;
	private static final int COL_DOI_TUONG = 9;
	private static final int COL_KHU_VUC = 10;
	private static final int COL_CHUONG_TRINH = 11;
	private static final int COL_DAN_TOC = 12;
	private static final int COL_MA_DAN_TOC = 13;
	private static final int COL_ACTION = 14;

	private final CandidateService candidateService;
	private final DefaultTableModel tableModel;
	private final JTable table;
	private final JTable fixedActionTable;

	private final JTextField txtSearchCccd;
	private final JTextField txtSearchName;

	private final JLabel lblPaging;
	private final JLabel lblRows;
	private final int actionColumnIndex;

	private int currentPage = 1;
	private int totalPages = 1;
	private List<CandidateDTO> currentCandidates = new ArrayList<>();

	public CandidatePanel() {
		this.candidateService = new CandidateService();
		this.tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == actionColumnIndex;
			}
		};
		this.table = new JTable(tableModel);
		this.fixedActionTable = new JTable(tableModel);

		this.txtSearchCccd = new JTextField(14);
		this.txtSearchName = new JTextField(18);

		this.lblPaging = new JLabel("Trang 1/1 (20 dòng/trang)");
		this.lblRows = new JLabel("Tổng dòng: 0");
		this.actionColumnIndex = tableModel.getColumnCount() - 1;

		attachLiveSearch();

		setLayout(new BorderLayout(8, 8));
		add(buildTopPanel(), BorderLayout.NORTH);
		add(buildTablePanel(), BorderLayout.CENTER);
		add(buildBottomPanel(), BorderLayout.SOUTH);

		loadPage(1);
	}

	private JPanel buildTopPanel() {
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));

		JPanel left = new JPanel();
		left.setLayout(new javax.swing.BoxLayout(left, javax.swing.BoxLayout.Y_AXIS));
		left.setOpaque(false);

		JLabel title = new JLabel("Quản lý thí sinh");
		title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 28f));
		title.setForeground(new Color(17, 24, 39));

		JLabel subtitle = new JLabel("Quản lý thông tin thí sinh tuyển sinh");
		subtitle.setFont(subtitle.getFont().deriveFont(java.awt.Font.PLAIN, 13f));
		subtitle.setForeground(new Color(75, 85, 99));

		left.add(title);
		left.add(javax.swing.Box.createVerticalStrut(4));
		left.add(subtitle);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		actions.setOpaque(false);
		JButton btnImport = new JButton("Import");
		JButton btnExport = new JButton("Export");
		JButton btnAdd = new JButton("+ Thêm thí sinh");
		styleButton(btnImport, COLOR_GREEN);
		styleButton(btnExport, COLOR_BLUE_SOFT);
		styleButton(btnAdd, COLOR_BLUE);
		btnImport.setMargin(new Insets(6, 16, 6, 16));
		btnExport.setMargin(new Insets(6, 16, 6, 16));
		btnAdd.setMargin(new Insets(6, 16, 6, 16));
		actions.add(btnImport);
		actions.add(btnExport);
		actions.add(btnAdd);

		btnImport.addActionListener(e -> importCandidatesFromExcel());
		btnExport.addActionListener(e -> exportCandidatesToExcel());
		btnAdd.addActionListener(e -> addCandidateFull());

		wrapper.add(left, BorderLayout.WEST);
		wrapper.add(actions, BorderLayout.EAST);
		return wrapper;
	}

	private JPanel buildTablePanel() {
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		table.setRowHeight(34);
		table.setSelectionBackground(new Color(227, 242, 253));
		table.setSelectionForeground(Color.BLACK);
		styleTableHeader(table);

		fixedActionTable.setSelectionModel(table.getSelectionModel());
		fixedActionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		fixedActionTable.getTableHeader().setReorderingAllowed(false);
		fixedActionTable.getTableHeader().setResizingAllowed(false);
		fixedActionTable.setRowHeight(34);
		fixedActionTable.setSelectionBackground(new Color(227, 242, 253));
		fixedActionTable.setSelectionForeground(Color.BLACK);
		styleTableHeader(fixedActionTable);

		applyFixedColumnWidths();

		// Main table scrolls horizontally; remove the action column from the main table view
		// by finding the column whose model index matches `actionColumnIndex`.
		int actionViewIndex = -1;
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			if (table.getColumnModel().getColumn(i).getModelIndex() == actionColumnIndex) {
				actionViewIndex = i;
				break;
			}
		}
		if (actionViewIndex >= 0) {
			table.removeColumn(table.getColumnModel().getColumn(actionViewIndex));
		}

		// Fixed table keeps only the action column and remains visible.
		for (int i = fixedActionTable.getColumnModel().getColumnCount() - 1; i >= 0; i--) {
			if (fixedActionTable.getColumnModel().getColumn(i).getModelIndex() != actionColumnIndex) {
				fixedActionTable.removeColumn(fixedActionTable.getColumnModel().getColumn(i));
			}
		}
		fixedActionTable.getColumnModel().getColumn(0).setCellRenderer(new ActionCellRenderer());
		fixedActionTable.getColumnModel().getColumn(0).setCellEditor(new ActionCellEditor());
		fixedActionTable.getColumnModel().getColumn(0).setPreferredWidth(96);
		fixedActionTable.getColumnModel().getColumn(0).setMinWidth(96);
		fixedActionTable.getColumnModel().getColumn(0).setMaxWidth(96);

		JScrollPane mainScroll = new JScrollPane(table);
		JScrollPane fixedScroll = new JScrollPane(fixedActionTable);
		mainScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		fixedScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		fixedScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		fixedScroll.getVerticalScrollBar().setModel(mainScroll.getVerticalScrollBar().getModel());
		fixedScroll.setPreferredSize(new Dimension(96, 0));
		mainScroll.setBorder(BorderFactory.createEmptyBorder());
		fixedScroll.setBorder(BorderFactory.createEmptyBorder());
		fixedScroll.setViewportBorder(BorderFactory.createEmptyBorder());

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBorder(BorderFactory.createEtchedBorder());
		wrapper.add(mainScroll, BorderLayout.CENTER);
		wrapper.add(fixedScroll, BorderLayout.EAST);

		JScrollBar sharedHorizontalBar = new JScrollBar(JScrollBar.HORIZONTAL);
		sharedHorizontalBar.setModel(mainScroll.getHorizontalScrollBar().getModel());
		wrapper.add(sharedHorizontalBar, BorderLayout.SOUTH);
		return wrapper;
	}

	private void applyFixedColumnWidths() {
		int[] widths = {70, 150, 120, 200, 100, 100, 80, 180, 140, 90, 90, 110, 100, 90, 96};
		for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMinWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMaxWidth(widths[i]);
		}
	}

	private JPanel buildBottomPanel() {
		JPanel wrapper = new JPanel(new BorderLayout(8, 8));
		wrapper.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

		JPanel searchCard = new JPanel(new BorderLayout());
		searchCard.setBackground(Color.WHITE);
		searchCard.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(227, 231, 239), 1),
				BorderFactory.createEmptyBorder(12, 14, 12, 14)
		));

		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		searchPanel.setOpaque(false);
		searchPanel.add(new JLabel("CCCD:"));
		searchPanel.add(txtSearchCccd);
		searchPanel.add(new JLabel("Họ tên:"));
		searchPanel.add(txtSearchName);
		searchCard.add(searchPanel, BorderLayout.CENTER);

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnPrev = new JButton("◀");
		JButton btnNext = new JButton("▶");
		styleButton(btnPrev, COLOR_BLUE);
		styleButton(btnNext, COLOR_BLUE);
		btnPrev.setToolTipText("Trang trước");
		btnNext.setToolTipText("Trang sau");
		btnPrev.setMargin(new Insets(2, 10, 2, 10));
		btnNext.setMargin(new Insets(2, 10, 2, 10));

		controls.add(lblRows);
		controls.add(lblPaging);
		controls.add(btnPrev);
		controls.add(btnNext);

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

		wrapper.add(searchCard, BorderLayout.CENTER);
		wrapper.add(controls, BorderLayout.SOUTH);
		return wrapper;
	}

	private void attachLiveSearch() {
		DocumentListener listener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				loadPage(1);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				loadPage(1);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				loadPage(1);
			}
		};

		txtSearchCccd.getDocument().addDocumentListener(listener);
		txtSearchName.getDocument().addDocumentListener(listener);
	}

	private void loadPage(int page) {
		try {
			String cccd = txtSearchCccd.getText();
			String name = txtSearchName.getText();

			int countedPages = candidateService.countPages(cccd, name);
			int safePage = Math.max(1, Math.min(page, countedPages));

			List<CandidateDTO> rows = candidateService.getCandidates(cccd, name, safePage);
			int totalRows = candidateService.countRows(cccd, name);
			currentCandidates = new ArrayList<>(rows);

			tableModel.setRowCount(0);
			for (int i = 0; i < rows.size(); i++) {
				CandidateDTO c = rows.get(i);
				int stt = (safePage - 1) * PAGE_SIZE + i + 1;
				tableModel.addRow(new Object[]{
						stt,
						c.getCccd(),
						c.getSoBaoDanh(),
						c.getHoTen(),
						c.getNgaySinh(),
						c.getDienThoai(),
						c.getGioiTinh(),
						c.getEmail(),
						c.getNoiSinh(),
						emptyIfNull(c.getDoiTuong()),
						emptyIfNull(c.getKhuVuc()),
						c.getChuongTrinh(),
						emptyIfNull(c.getDanToc()),
						emptyIfNull(c.getMaDanToc()),
						""
				});
			}

			this.currentPage = safePage;
			this.totalPages = countedPages;
			this.lblPaging.setText("Trang " + currentPage + "/" + totalPages + " (20 dòng/trang)");
			this.lblRows.setText("Tổng dòng: " + totalRows);
		} catch (SQLException ex) {
			showError("Không thể tải danh sách thí sinh", ex);
		}
	}

	private void editCandidateAt(int row) {
		CandidateDTO candidate = new CandidateDTO();
		int idThisinh = getIdThisinhAtRow(row);
		if (idThisinh < 0) {
			JOptionPane.showMessageDialog(this, "Không xác định được thí sinh để sửa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			return;
		}
		candidate.setIdThisinh(idThisinh);
		candidate.setCccd(stringValueAt(row, COL_CCCD));
		candidate.setSoBaoDanh(stringValueAt(row, COL_SO_BAO_DANH));
		String[] nameParts = splitFullName(stringValueAt(row, COL_HO_TEN));
		candidate.setHo(nameParts[0]);
		candidate.setTen(nameParts[1]);
		candidate.setNgaySinh(stringValueAt(row, COL_NGAY_SINH));
		candidate.setDienThoai(stringValueAt(row, COL_DIEN_THOAI));
		candidate.setGioiTinh(stringValueAt(row, COL_GIOI_TINH));
		candidate.setEmail(stringValueAt(row, COL_EMAIL));
		candidate.setNoiSinh(stringValueAt(row, COL_NOI_SINH));
		candidate.setDoiTuong(stringValueAt(row, COL_DOI_TUONG));
		candidate.setKhuVuc(stringValueAt(row, COL_KHU_VUC));
		candidate.setChuongTrinh(stringValueAt(row, COL_CHUONG_TRINH));
		candidate.setDanToc(stringValueAt(row, COL_DAN_TOC));
		candidate.setMaDanToc(stringValueAt(row, COL_MA_DAN_TOC));

		CandidateDTO edited = CandidateFormDialog.showDialog(this, candidate, true);
		if (edited == null) {
			return;
		}
		edited.setIdThisinh(candidate.getIdThisinh());
		edited.setSoBaoDanh("SBD" + edited.getCccd());

		try {
			boolean updated = candidateService.updateCandidateFull(edited);
			if (updated) {
				JOptionPane.showMessageDialog(this, "Cập nhật thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
				loadPage(currentPage);
			} else {
				JOptionPane.showMessageDialog(this, "Cập nhật thất bại. Vui lòng kiểm tra dữ liệu bắt buộc.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			}
		} catch (SQLException ex) {
			showError("Không thể cập nhật thí sinh", ex);
		}
	}

	private void addCandidateFull() {
		CandidateDTO draft = new CandidateDTO();
		CandidateDTO candidate = CandidateFormDialog.showDialog(this, draft, false);
		if (candidate == null) {
			return;
		}
		candidate.setSoBaoDanh("SBD" + candidate.getCccd());

		try {
			boolean created = candidateService.createCandidateFull(candidate);
			if (created) {
				JOptionPane.showMessageDialog(this, "Thêm thí sinh thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
				loadPage(Integer.MAX_VALUE);
			} else {
				JOptionPane.showMessageDialog(this, "Thêm thất bại. Kiểm tra CCCD/Tên.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			}
		} catch (SQLException ex) {
			showError("Không thể thêm thí sinh", ex);
		}
	}

	private void importCandidatesFromExcel() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Chọn file Excel thí sinh");
		chooser.setFileFilter(new FileNameExtensionFilter("Excel files (*.xlsx)", "xlsx"));

		int result = chooser.showOpenDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		try {
			List<CandidateDTO> imported = CandidateExcelImportUtil.importCandidates(file);
			if (imported.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Không có dữ liệu hợp lệ trong file.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			int success = 0;
			int fail = 0;
			String firstError = null;
			for (CandidateDTO candidate : imported) {
				if (candidate.getSoBaoDanh() == null || candidate.getSoBaoDanh().trim().isEmpty()) {
					candidate.setSoBaoDanh("SBD" + candidate.getCccd());
				}
				try {
					if (candidateService.upsertCandidateFull(candidate)) {
						success++;
					} else {
						fail++;
					}
				} catch (Exception ex) {
					fail++;
					if (firstError == null) {
						firstError = ex.getMessage();
					}
				}
			}

			String message = "Import xong: " + success + " dòng thành công, " + fail + " dòng lỗi.";
			if (firstError != null && !firstError.trim().isEmpty()) {
				message += "\nLỗi đầu tiên: " + firstError;
			}

			JOptionPane.showMessageDialog(
					this,
					message,
					"Kết quả import",
					JOptionPane.INFORMATION_MESSAGE
			);
			loadPage(1);
		} catch (Exception ex) {
			showError("Không thể import file Excel", ex instanceof Exception ? (Exception) ex : new Exception(ex));
		}
	}

	private void deleteCandidateAt(int row) {
		int confirm = JOptionPane.showConfirmDialog(
				this,
				"Bạn có chắc chắn muốn xóa thí sinh đang chọn?",
				"Xác nhận xóa",
				JOptionPane.YES_NO_OPTION
		);
		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		int idThisinh = getIdThisinhAtRow(row);
		if (idThisinh < 0) {
			JOptionPane.showMessageDialog(this, "Không xác định được thí sinh để xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			boolean deleted = candidateService.deleteCandidateById(idThisinh);
			if (deleted) {
				JOptionPane.showMessageDialog(this, "Xóa thí sinh thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
				loadPage(currentPage);
			} else {
				JOptionPane.showMessageDialog(this, "Xóa thất bại.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			}
		} catch (SQLException ex) {
			showError("Không thể xóa thí sinh", ex);
		}
	}

	private void exportCandidatesToExcel() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Lưu file Excel thí sinh");
		chooser.setFileFilter(new FileNameExtensionFilter("Excel files (*.xlsx)", "xlsx"));
		chooser.setSelectedFile(new File("thi-sinh-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".xlsx"));

		int result = chooser.showSaveDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		if (!file.getName().toLowerCase().endsWith(".xlsx")) {
			file = new File(file.getParentFile(), file.getName() + ".xlsx");
		}

		try {
			List<CandidateDTO> rows = getAllCandidatesByCurrentFilter();
			if (rows.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Không có dữ liệu để export.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			CandidateExcelExportUtil.exportCandidates(file, rows);
			JOptionPane.showMessageDialog(
					this,
					"Export thành công " + rows.size() + " dòng.\nFile: " + file.getAbsolutePath(),
					"Kết quả export",
					JOptionPane.INFORMATION_MESSAGE
			);
		} catch (Exception ex) {
			showError("Không thể export file Excel", ex instanceof Exception ? (Exception) ex : new Exception(ex));
		}
	}

	private List<CandidateDTO> getAllCandidatesByCurrentFilter() throws SQLException {
		String cccd = txtSearchCccd.getText();
		String name = txtSearchName.getText();
		int pages = candidateService.countPages(cccd, name);

		List<CandidateDTO> all = new ArrayList<>();
		for (int p = 1; p <= pages; p++) {
			all.addAll(candidateService.getCandidates(cccd, name, p));
		}
		return all;
	}

	private String stringValueAt(int row, int col) {
		Object value = tableModel.getValueAt(row, col);
		return value == null ? "" : value.toString();
	}

	private int getIdThisinhAtRow(int row) {
		if (row < 0 || row >= currentCandidates.size()) {
			return -1;
		}
		Integer id = currentCandidates.get(row).getIdThisinh();
		return id == null ? -1 : id;
	}

	private void showError(String title, Exception ex) {
		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
				this,
				title + ": " + ex.getMessage(),
				"Lỗi",
				JOptionPane.ERROR_MESSAGE
		));
	}

	private String formatNumber(Double value) {
		if (value == null) {
			return "";
		}
		return String.format("%.2f", value);
	}

	private String emptyIfNull(String value) {
		return value == null ? "" : value;
	}

	private Double parseDoubleOrNull(String value) {
		if (value == null) {
			return null;
		}
		String t = value.trim();
		if (t.isEmpty()) {
			return null;
		}
		try {
			return Double.parseDouble(t);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private String[] splitFullName(String fullName) {
		if (fullName == null || fullName.trim().isEmpty()) {
			return new String[]{"", ""};
		}
		String normalized = fullName.trim().replaceAll("\\s+", " ");
		int lastSpace = normalized.lastIndexOf(' ');
		if (lastSpace < 0) {
			return new String[]{"", normalized};
		}
		String hoPart = normalized.substring(0, lastSpace).trim();
		String tenPart = normalized.substring(lastSpace + 1).trim();
		return new String[]{hoPart, tenPart};
	}

	private void styleButton(JButton button, Color background) {
		button.setBackground(background);
		button.setForeground(Color.WHITE);
		button.setFocusPainted(false);
		button.setOpaque(true);
		button.setBorderPainted(false);
	}

	private void styleTableHeader(JTable targetTable) {
		JTableHeader header = targetTable.getTableHeader();
		header.setBackground(COLOR_BLUE);
		header.setForeground(Color.WHITE);
		header.setOpaque(true);
	}

	private class ActionCellRenderer extends JPanel implements TableCellRenderer {
		private final JButton btnEdit;
		private final JButton btnDelete;

		ActionCellRenderer() {
			setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
			btnEdit = new JButton("✎");
			btnDelete = new JButton("🗑");
			styleButton(btnEdit, COLOR_GREEN);
			styleButton(btnDelete, COLOR_RED);
			btnEdit.setMargin(new Insets(2, 6, 2, 6));
			btnDelete.setMargin(new Insets(2, 6, 2, 6));
			btnEdit.setFocusable(false);
			btnDelete.setFocusable(false);
			btnEdit.setToolTipText("Sửa");
			btnDelete.setToolTipText("Xóa");
			add(btnEdit);
			add(btnDelete);
		}

		@Override
		public Component getTableCellRendererComponent(
				JTable table,
				Object value,
				boolean isSelected,
				boolean hasFocus,
				int row,
				int column
		) {
			setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			return this;
		}
	}

	private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
		private final JPanel panel;
		private final JButton btnEdit;
		private final JButton btnDelete;
		private int currentRow = -1;

		ActionCellEditor() {
			panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
			btnEdit = new JButton("✎");
			btnDelete = new JButton("🗑");
			styleButton(btnEdit, COLOR_GREEN);
			styleButton(btnDelete, COLOR_RED);
			btnEdit.setMargin(new Insets(2, 6, 2, 6));
			btnDelete.setMargin(new Insets(2, 6, 2, 6));
			btnEdit.setFocusable(false);
			btnDelete.setFocusable(false);
			btnEdit.setToolTipText("Sửa");
			btnDelete.setToolTipText("Xóa");
			panel.add(btnEdit);
			panel.add(btnDelete);

			btnEdit.addActionListener(e -> {
				fireEditingStopped();
				if (currentRow >= 0) {
					editCandidateAt(currentRow);
				}
			});

			btnDelete.addActionListener(e -> {
				fireEditingStopped();
				if (currentRow >= 0) {
					deleteCandidateAt(currentRow);
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			this.currentRow = row;
			panel.setBackground(table.getSelectionBackground());
			return panel;
		}

		@Override
		public Object getCellEditorValue() {
			return "";
		}
	}
}
