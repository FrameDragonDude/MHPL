package gui.panels;

import bus.BonusPointService;
import dto.BonusPointDTO;
import gui.dialogs.BonusPointEditDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import utils.excel.BonusPointExcelImportUtil;

public class BonusPointPanel extends JPanel {
	private static final Color COLOR_GREEN = new Color(46, 125, 50);
	private static final Color COLOR_BLUE = new Color(21, 101, 192);
	private static final Color COLOR_BLUE_SOFT = new Color(30, 136, 229);
	private static final Color COLOR_RED = new Color(198, 40, 40);
	private static final int PAGE_SIZE = 20;

	private static final String[] TABLE_COLUMNS = {
			"STT", "CCCD", "Chứng chỉ ngoại ngữ", "Điểm", "Điểm quy đổi", "Điểm cộng", "Thao tác"
	};

	private final BonusPointService service;
	private final DefaultTableModel tableModel;
	private final JTable table;
	private final JTable fixedActionTable;
	private final JTextField txtSearchCccd;
	private final JTextField txtSearchMethod;
	private final JLabel lblPaging;
	private final JLabel lblRows;
	private final int actionColumnIndex;
	private int currentPage = 1;
	private int totalPages = 1;
	private List<BonusPointDTO> currentRows = new ArrayList<>();

	public BonusPointPanel() {
		this.service = new BonusPointService();
		this.tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == actionColumnIndex;
			}
		};
		this.table = new JTable(tableModel);
		this.fixedActionTable = new JTable(tableModel);
		this.txtSearchCccd = new JTextField(14);
		this.txtSearchMethod = new JTextField(18);
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

		JLabel title = new JLabel("Quản lý điểm cộng");
		title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 28f));
		title.setForeground(new Color(17, 24, 39));

		JLabel subtitle = new JLabel("Danh sách CCCD, chứng chỉ ngoại ngữ và điểm cộng");
		subtitle.setFont(subtitle.getFont().deriveFont(java.awt.Font.PLAIN, 13f));
		subtitle.setForeground(new Color(75, 85, 99));

		left.add(title);
		left.add(javax.swing.Box.createVerticalStrut(4));
		left.add(subtitle);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		actions.setOpaque(false);
		JButton btnImport = new JButton("Import");
		JButton btnAdd = new JButton("+ Thêm điểm cộng");
		styleButton(btnImport, COLOR_GREEN);
		styleButton(btnAdd, COLOR_BLUE);
		btnImport.setMargin(new Insets(6, 16, 6, 16));
		btnAdd.setMargin(new Insets(6, 16, 6, 16));
		actions.add(btnImport);
		actions.add(btnAdd);

		btnImport.addActionListener(e -> importFromExcel());
		btnAdd.addActionListener(e -> addRow());

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

		table.removeColumn(table.getColumnModel().getColumn(actionColumnIndex));
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
		searchPanel.add(new JLabel("Chứng chỉ ngoại ngữ:"));
		searchPanel.add(txtSearchMethod);
		searchCard.add(searchPanel, BorderLayout.CENTER);

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnPrev = new JButton("◀");
		JButton btnNext = new JButton("▶");
		styleButton(btnPrev, COLOR_BLUE);
		styleButton(btnNext, COLOR_BLUE);
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

	private void applyFixedColumnWidths() {
		int[] widths = {70, 150, 220, 90, 110, 100, 96};
		for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMinWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMaxWidth(widths[i]);
		}
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
		txtSearchMethod.getDocument().addDocumentListener(listener);
	}

	private void loadPage(int page) {
		try {
			String cccd = txtSearchCccd.getText();
			String method = txtSearchMethod.getText();
			int countedPages = service.countPages(cccd, method);
			int safePage = Math.max(1, Math.min(page, countedPages));
			List<BonusPointDTO> rows = service.getRows(cccd, method, safePage);
			int totalRows = service.countRows(cccd, method);
			currentRows = new ArrayList<>(rows);

			tableModel.setRowCount(0);
			for (int i = 0; i < rows.size(); i++) {
				BonusPointDTO row = rows.get(i);
				int stt = (safePage - 1) * PAGE_SIZE + i + 1;
				tableModel.addRow(new Object[]{
						stt,
						emptyIfNull(row.getTsCccd()),
						emptyIfNull(row.getPhuongThuc()),
						formatNumber(row.getDiemCC()),
						formatNumber(row.getDiemUtxt()),
						formatNumber(row.getDiemTong()),
						""
				});
			}

			this.currentPage = safePage;
			this.totalPages = countedPages;
			this.lblPaging.setText("Trang " + currentPage + "/" + totalPages + " (20 dòng/trang)");
			this.lblRows.setText("Tổng dòng: " + totalRows);
		} catch (SQLException ex) {
			showError("Không thể tải danh sách điểm cộng", ex);
		}
	}

	private void addRow() {
		BonusPointDTO draft = new BonusPointDTO();
		BonusPointDTO dto = BonusPointEditDialog.showDialog(this, draft, false);
		if (dto == null) {
			return;
		}
		try {
			boolean created = service.create(dto);
			if (created) {
				JOptionPane.showMessageDialog(this, "Thêm điểm cộng thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
				loadPage(Integer.MAX_VALUE);
			} else {
				JOptionPane.showMessageDialog(this, "Thêm thất bại.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			}
		} catch (SQLException ex) {
			showError("Không thể thêm điểm cộng", ex);
		}
	}

	private void editRow(int rowIndex) {
		BonusPointDTO current = getCurrentRow(rowIndex);
		if (current == null) {
			return;
		}
		BonusPointDTO edited = BonusPointEditDialog.showDialog(this, current, true);
		if (edited == null) {
			return;
		}
		edited.setId(current.getId());
		try {
			boolean updated = service.update(edited);
			if (updated) {
				JOptionPane.showMessageDialog(this, "Cập nhật thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
				loadPage(currentPage);
			} else {
				JOptionPane.showMessageDialog(this, "Cập nhật thất bại.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			}
		} catch (SQLException ex) {
			showError("Không thể cập nhật điểm cộng", ex);
		}
	}

	private void deleteRow(int rowIndex) {
		BonusPointDTO current = getCurrentRow(rowIndex);
		if (current == null || current.getId() == null) {
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bản ghi điểm cộng đang chọn?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		try {
			boolean deleted = service.deleteById(current.getId());
			if (deleted) {
				JOptionPane.showMessageDialog(this, "Xóa thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
				loadPage(currentPage);
			} else {
				JOptionPane.showMessageDialog(this, "Xóa thất bại.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			}
		} catch (SQLException ex) {
			showError("Không thể xóa điểm cộng", ex);
		}
	}

	private void importFromExcel() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Chọn file Excel điểm cộng");
		chooser.setFileFilter(new FileNameExtensionFilter("Excel files (*.xlsx)", "xlsx"));

		int result = chooser.showOpenDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = chooser.getSelectedFile();
		try {
			List<BonusPointDTO> imported = BonusPointExcelImportUtil.importRows(file);
			if (imported.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Không có dữ liệu hợp lệ trong file.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			int success = 0;
			int fail = 0;
			String firstError = null;
			for (BonusPointDTO row : imported) {
				try {
					if (service.upsertByKey(row)) {
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
			JOptionPane.showMessageDialog(this, message, "Kết quả import", JOptionPane.INFORMATION_MESSAGE);
			loadPage(1);
		} catch (Exception ex) {
			showError("Không thể import file Excel", ex);
		}
	}

	private BonusPointDTO getCurrentRow(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= currentRows.size()) {
			return null;
		}
		return currentRows.get(rowIndex);
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

	private String emptyIfNull(String value) {
		return value == null ? "" : value;
	}

	private String formatNumber(Double value) {
		if (value == null) {
			return "";
		}
		if (value == value.longValue()) {
			return String.valueOf(value.longValue());
		}
		return String.valueOf(value);
	}

	private void showError(String title, Exception ex) {
		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, title + ": " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
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
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
			panel.add(btnEdit);
			panel.add(btnDelete);

			btnEdit.addActionListener(e -> {
				fireEditingStopped();
				if (currentRow >= 0) {
					editRow(currentRow);
				}
			});

			btnDelete.addActionListener(e -> {
				fireEditingStopped();
				if (currentRow >= 0) {
					deleteRow(currentRow);
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			this.currentRow = row;
			panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			return panel;
		}

		@Override
		public Object getCellEditorValue() {
			return "";
		}
	}
}