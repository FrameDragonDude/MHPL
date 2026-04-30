package gui.panels;

import bus.ConversionRuleService;
import dto.ConversionRuleDTO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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

public class ConversionRulePanel extends JPanel {
	private static final Color COLOR_BLUE = new Color(21, 101, 192);
	private static final String[] TABLE_COLUMNS = {
		"Phương thức", "Tổ hợp", "Môn", "Điểm A", "Điểm B", "Điểm C", "Điểm D", "Mã quy đổi", "Phân vị"
	};

	private final ConversionRuleService service;
	private final DefaultTableModel tableModel;
	private final JTable table;
	private final JTextField txtSearchPhuongThuc;
	private final JTextField txtSearchToHop;
	private final JLabel lblPaging;
	private final JLabel lblRows;
	private int currentPage = 1;
	private int totalPages = 1;
	private List<ConversionRuleDTO> currentRows = new ArrayList<>();

	public ConversionRulePanel() {
		this.service = new ConversionRuleService();
		this.tableModel = new DefaultTableModel(TABLE_COLUMNS, 0);
		this.table = new JTable(tableModel);
		this.txtSearchPhuongThuc = new JTextField(14);
		this.txtSearchToHop = new JTextField(16);
		this.lblPaging = new JLabel("Trang 1/1 (20 dòng/trang)");
		this.lblRows = new JLabel("Tổng dòng: 0");

		setLayout(new BorderLayout(8, 8));
		add(buildTopPanel(), BorderLayout.NORTH);
		add(buildTablePanel(), BorderLayout.CENTER);
		add(buildBottomPanel(), BorderLayout.SOUTH);

		attachLiveSearch();
		loadPage(1);
	}

	private JPanel buildTopPanel() {
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));

		JPanel left = new JPanel();
		left.setLayout(new javax.swing.BoxLayout(left, javax.swing.BoxLayout.Y_AXIS));
		left.setOpaque(false);

		JLabel title = new JLabel("Bảng quy đổi");
		title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 28f));
		title.setForeground(new Color(17, 24, 39));

		JLabel subtitle = new JLabel("Dữ liệu quy đổi đã có sẵn trong cơ sở dữ liệu");
		subtitle.setFont(subtitle.getFont().deriveFont(java.awt.Font.PLAIN, 13f));
		subtitle.setForeground(new Color(75, 85, 99));

		left.add(title);
		left.add(javax.swing.Box.createVerticalStrut(4));
		left.add(subtitle);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		actions.setOpaque(false);
		JButton btnRefresh = new JButton("Làm mới");
		btnRefresh.setBackground(COLOR_BLUE);
		btnRefresh.setForeground(Color.WHITE);
		btnRefresh.setFocusPainted(false);
		btnRefresh.setBorderPainted(false);
		btnRefresh.setMargin(new Insets(6, 16, 6, 16));
		btnRefresh.addActionListener(e -> loadPage(currentPage));
		actions.add(btnRefresh);

		wrapper.add(left, BorderLayout.WEST);
		wrapper.add(actions, BorderLayout.EAST);
		return wrapper;
	}

	private JPanel buildTablePanel() {
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getTableHeader().setReorderingAllowed(false);
		table.setRowHeight(32);
		styleTableHeader(table);

		int[] widths = {110, 110, 90, 90, 90, 90, 90, 140, 90};
		for (int i = 0; i < widths.length; i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMinWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMaxWidth(widths[i]);
		}

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBorder(BorderFactory.createEtchedBorder());
		wrapper.add(scrollPane, BorderLayout.CENTER);
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
		searchPanel.add(new JLabel("Phương thức:"));
		searchPanel.add(txtSearchPhuongThuc);
		searchPanel.add(new JLabel("Tổ hợp:"));
		searchPanel.add(txtSearchToHop);
		searchCard.add(searchPanel, BorderLayout.CENTER);

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnPrev = new JButton("◀");
		JButton btnNext = new JButton("▶");
		styleButton(btnPrev);
		styleButton(btnNext);
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
			public void insertUpdate(DocumentEvent e) { loadPage(1); }

			@Override
			public void removeUpdate(DocumentEvent e) { loadPage(1); }

			@Override
			public void changedUpdate(DocumentEvent e) { loadPage(1); }
		};
		txtSearchPhuongThuc.getDocument().addDocumentListener(listener);
		txtSearchToHop.getDocument().addDocumentListener(listener);
	}

	private void loadPage(int page) {
		try {
			String phuongThuc = txtSearchPhuongThuc.getText();
			String toHop = txtSearchToHop.getText();
			int countedPages = service.countPages(phuongThuc, toHop);
			int safePage = Math.max(1, Math.min(page, countedPages));
			List<ConversionRuleDTO> rows = service.getRows(phuongThuc, toHop, safePage);
			int totalRows = service.countRows(phuongThuc, toHop);
			currentRows = new ArrayList<>(rows);

			tableModel.setRowCount(0);
			for (ConversionRuleDTO row : rows) {
				tableModel.addRow(new Object[]{
					emptyIfNull(row.getPhuongThuc()),
					emptyIfNull(row.getToHop()),
					emptyIfNull(row.getMon()),
					emptyIfNull(row.getDiemA()),
					emptyIfNull(row.getDiemB()),
					emptyIfNull(row.getDiemC()),
					emptyIfNull(row.getDiemD()),
					emptyIfNull(row.getMaQuyDoi()),
					emptyIfNull(row.getPhanVi())
				});
			}

			currentPage = safePage;
			totalPages = countedPages;
			lblPaging.setText("Trang " + currentPage + "/" + totalPages + " (20 dòng/trang)");
			lblRows.setText("Tổng dòng: " + totalRows);
		} catch (SQLException ex) {
			showError("Không thể tải bảng quy đổi", ex);
		}
	}

	private void styleTableHeader(JTable targetTable) {
		JTableHeader header = targetTable.getTableHeader();
		header.setBackground(COLOR_BLUE);
		header.setForeground(Color.WHITE);
		header.setOpaque(true);
	}

	private void styleButton(JButton button) {
		button.setBackground(COLOR_BLUE);
		button.setForeground(Color.WHITE);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
	}

	private String emptyIfNull(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof Double doubleValue) {
			if (doubleValue == doubleValue.longValue()) {
				return String.valueOf(doubleValue.longValue());
			}
			return String.valueOf(doubleValue);
		}
		return value.toString();
	}

	private void showError(String title, Exception ex) {
		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, title + ": " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
	}
}