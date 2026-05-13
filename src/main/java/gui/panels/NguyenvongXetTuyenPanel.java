package gui.panels;

import bus.NguyenVongXetTuyenService;
import dto.NguyenVongXetTuyenDTO;
import gui.dialogs.NguyenVongXetTuyenEditDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class NguyenvongXetTuyenPanel extends JPanel {
	private static final Color COLOR_GREEN = new Color(46, 125, 50);
	private static final Color COLOR_BLUE = new Color(21, 101, 192);
	private static final Color COLOR_RED = new Color(198, 40, 40);
	private static final int PAGE_SIZE = 20;

	private static final String[] TABLE_COLUMNS = {
		"IDNV", "CCCD", "Mã ngành", "TT", "Điểm THXT", "Điểm UTQD", "Điểm cộng", "Điểm xét tuyển", "Kết quả", "Keys", "Phương thức", "THM", "Thao tác"
	};

	private static final int COL_IDNV = 0;
	private static final int COL_CCCD = 1;
	private static final int COL_MANGANH = 2;
	private static final int COL_TT = 3;
	private static final int COL_DIEM_THXT = 4;
	private static final int COL_DIEM_UTQD = 5;
	private static final int COL_DIEM_CONG = 6;
	private static final int COL_DIEM_XETTUYEN = 7;
	private static final int COL_KETQUA = 8;
	private static final int COL_KEYS = 9;
	private static final int COL_PHUONGTHUC = 10;
	private static final int COL_THM = 11;
	private static final int COL_ACTION = 12;

	private final NguyenVongXetTuyenService service;
	private final DefaultTableModel tableModel;
	private final JTable table;
	private final JTable fixedActionTable;
	private final JTextField txtSearchCccd;
	private final JTextField txtSearchMaNganh;
	private final JLabel lblPaging;
	private final JLabel lblRows;
	private final JButton btnGenerate;
	private final int actionColumnIndex;
	private int currentPage = 1;
	private int totalPages = 1;
	private List<NguyenVongXetTuyenDTO> currentRows = new ArrayList<>();

	public NguyenvongXetTuyenPanel() {
		this.service = new NguyenVongXetTuyenService();
		this.tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == actionColumnIndex;
			}
		};
		this.table = new JTable(tableModel);
		this.fixedActionTable = new JTable(tableModel);
		this.txtSearchCccd = new JTextField(14);
		this.txtSearchMaNganh = new JTextField(16);
		this.lblPaging = new JLabel("Trang 1/1 (20 dòng/trang)");
		this.lblRows = new JLabel("Tổng dòng: 0");
		this.btnGenerate = new JButton("Sinh dữ liệu từ DB");
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

		JLabel title = new JLabel("Quản lý nguyện vọng xét tuyển");
		title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 28f));
		title.setForeground(new Color(17, 24, 39));

		JLabel subtitle = new JLabel("Danh sách nguyện vọng xét tuyển với điểm tuyên sinh");
		subtitle.setFont(subtitle.getFont().deriveFont(java.awt.Font.PLAIN, 13f));
		subtitle.setForeground(new Color(75, 85, 99));

		left.add(title);
		left.add(javax.swing.Box.createVerticalStrut(4));
		left.add(subtitle);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		actions.setOpaque(false);
		JButton btnAdd = new JButton("+ Thêm nguyện vọng");
		styleButton(btnAdd, COLOR_BLUE);
		btnAdd.setMargin(new Insets(6, 16, 6, 16));
		styleButton(btnGenerate, COLOR_GREEN);
		btnGenerate.setMargin(new Insets(6, 16, 6, 16));
		actions.add(btnAdd);
		actions.add(btnGenerate);

		btnAdd.addActionListener(e -> addRow());
		btnGenerate.addActionListener(e -> generateFromDatabase());

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
		searchPanel.add(new JLabel("Mã ngành:"));
		searchPanel.add(txtSearchMaNganh);
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
		int[] widths = {70, 120, 100, 50, 90, 90, 90, 100, 100, 90, 100, 70, 96};
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
		txtSearchMaNganh.getDocument().addDocumentListener(listener);
	}

	private void loadPage(int page) {
		try {
			String cccd = txtSearchCccd.getText();
			String nganh = txtSearchMaNganh.getText();
			int countedPages = service.countPages(cccd, nganh);
			int safePage = Math.max(1, Math.min(page, countedPages));
			List<NguyenVongXetTuyenDTO> rows = service.getRows(cccd, nganh, safePage);
			int totalRows = service.countRows(cccd, nganh);
			currentRows = new ArrayList<>(rows);

			tableModel.setRowCount(0);
			for (NguyenVongXetTuyenDTO row : rows) {
				tableModel.addRow(new Object[]{
					emptyIfNull(row.getIdnv()),
					emptyIfNull(row.getNnCccd()),
					emptyIfNull(row.getNvManganh()),
					emptyIfNull(row.getNvTt()),
					emptyIfNull(row.getDiemThxt()),
					emptyIfNull(row.getDiemUtqd()),
					emptyIfNull(row.getDiemCong()),
					emptyIfNull(row.getDiemXettuyen()),
					emptyIfNull(row.getNvKetqua()),
					emptyIfNull(row.getNvKeys()),
					emptyIfNull(row.getTtPhuongthuc()),
					emptyIfNull(row.getTtThm()),
					""
				});
			}

			this.currentPage = safePage;
			this.totalPages = countedPages;
			this.lblPaging.setText("Trang " + currentPage + "/" + totalPages + " (20 dòng/trang)");
			this.lblRows.setText("Tổng dòng: " + totalRows);
		} catch (SQLException ex) {
			showError("Không thể tải danh sách nguyện vọng xét tuyển", ex);
		}
	}

	private void addRow() {
		NguyenVongXetTuyenDTO draft = new NguyenVongXetTuyenDTO();
		NguyenVongXetTuyenDTO dto = showDialog(draft, false);
		if (dto == null) {
			return;
		}
		try {
			boolean created = service.create(dto);
			if (created) {
				JOptionPane.showMessageDialog(this, "Thêm nguyện vọng thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
				loadPage(Integer.MAX_VALUE);
			} else {
				JOptionPane.showMessageDialog(this, "Thêm thất bại.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			}
		} catch (SQLException ex) {
			showError("Không thể thêm nguyện vọng xét tuyển", ex);
		}
	}

	private void editRow(int rowIndex) {
		NguyenVongXetTuyenDTO current = getCurrentRow(rowIndex);
		if (current == null) {
			return;
		}
		NguyenVongXetTuyenDTO edited = showDialog(current, false);
		if (edited == null) {
			return;
		}
		edited.setIdnv(current.getIdnv());
		try {
			boolean updated = service.update(edited);
			if (updated) {
				JOptionPane.showMessageDialog(this, "Cập nhật thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
				loadPage(currentPage);
			} else {
				JOptionPane.showMessageDialog(this, "Cập nhật thất bại.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			}
		} catch (SQLException ex) {
			showError("Không thể cập nhật nguyện vọng xét tuyển", ex);
		}
	}

	private void deleteRow(int rowIndex) {
		NguyenVongXetTuyenDTO current = getCurrentRow(rowIndex);
		if (current == null || current.getIdnv() == null) {
			return;
		}

		int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nguyện vọng xét tuyển?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		try {
			boolean deleted = service.deleteById(current.getIdnv());
			if (deleted) {
				JOptionPane.showMessageDialog(this, "Xóa thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
				loadPage(currentPage);
			} else {
				JOptionPane.showMessageDialog(this, "Xóa thất bại.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
			}
		} catch (SQLException ex) {
			showError("Không thể xóa nguyện vọng xét tuyển", ex);
		}
	}

	private void generateFromDatabase() {
		int confirm = JOptionPane.showConfirmDialog(
				this,
				"Sinh lại dữ liệu nguyện vọng xét tuyển từ toàn bộ dữ liệu trong DB?",
				"Xác nhận sinh dữ liệu",
				JOptionPane.YES_NO_OPTION
		);
		if (confirm != JOptionPane.YES_OPTION) {
			return;
		}

		try {
			NguyenVongXetTuyenService.GenerationResult result = service.generateFromDatabase(true);
			JOptionPane.showMessageDialog(this, result.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
			loadPage(1);
		} catch (SQLException ex) {
			showError("Không thể sinh dữ liệu nguyện vọng xét tuyển", ex);
		}
	}

	private NguyenVongXetTuyenDTO getCurrentRow(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= currentRows.size()) {
			return null;
		}
		return currentRows.get(rowIndex);
	}

	private NguyenVongXetTuyenDTO showDialog(NguyenVongXetTuyenDTO dto, boolean isReadOnly) {
		JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
		NguyenVongXetTuyenEditDialog dialog = new NguyenVongXetTuyenEditDialog(parentFrame, dto);
		return dialog.showDialog();
	}

	private void showError(String title, Exception ex) {
		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, title + ": " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
	}

	private String emptyIfNull(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof java.math.BigDecimal bd) {
			return bd.stripTrailingZeros().toPlainString();
		}
		return value.toString();
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
