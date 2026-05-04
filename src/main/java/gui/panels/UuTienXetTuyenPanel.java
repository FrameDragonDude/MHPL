package gui.panels;

import bus.UuTienXetTuyenService;
import bus.AuditLogService;
import dto.UuTienXetTuyenDTO;
import dto.AuditLogDTO;
import gui.SessionManager;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import utils.excel.UuTienXetTuyenExcelImportUtil;

public class UuTienXetTuyenPanel extends JPanel {
    private static final Color COLOR_GREEN = new Color(46, 125, 50);
    private static final Color COLOR_BLUE = new Color(21, 101, 192);
    private static final Color COLOR_RED = new Color(198, 40, 40);
    private static final int PAGE_SIZE = 20;

        private static final String[] TABLE_COLUMNS = {
            "STT", "CCCD", "Cấp", "ĐT", "Mã môn", "Loại giải", "Điểm cộng cho môn đạt giải", "Điểm cộng cho THXT ko có môn đạt giải", "Có C/C", "Thao tác"
        };

    private final UuTienXetTuyenService service;
    private final AuditLogService auditLogService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTable fixedActionTable;
    private final JTextField txtSearchCccd;
    private final JTextField txtSearchGiai;
    private final JLabel lblPaging;
    private final int actionColumnIndex;
    private int currentPage = 1;
    private int totalPages = 1;
    private List<UuTienXetTuyenDTO> currentRows = new ArrayList<>();

    public UuTienXetTuyenPanel() {
        this.service = new UuTienXetTuyenService();
        this.auditLogService = new AuditLogService();
        this.tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == actionColumnIndex;
            }
        };
        this.table = new JTable(tableModel);
        this.fixedActionTable = new JTable(tableModel);
        this.txtSearchCccd = new JTextField(14);
        this.txtSearchGiai = new JTextField(18);
        this.lblPaging = new JLabel("Trang 1/1 (20 dòng/trang)");
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
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("Quản lý Ưu Tiên Xét Tuyển");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setForeground(new Color(17, 24, 39));

        JLabel subtitle = new JLabel("Danh sách ưu tiên xét tuyển thí sinh");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 13f));
        subtitle.setForeground(new Color(75, 85, 99));

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnImport = new JButton("Import Excel");
        JButton btnAdd = new JButton("+ Thêm");
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

    private JScrollPane buildTablePanel() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(34);
        styleTableHeader(table);
        table.setSelectionBackground(new Color(227, 242, 253));
        table.setSelectionForeground(Color.BLACK);
        fixedActionTable.setSelectionModel(table.getSelectionModel());
        fixedActionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        fixedActionTable.getTableHeader().setReorderingAllowed(false);
        fixedActionTable.setRowHeight(34);
        fixedActionTable.setSelectionBackground(new Color(227, 242, 253));
        fixedActionTable.setSelectionForeground(Color.BLACK);
        styleTableHeader(fixedActionTable);

        applyColumnWidths();

        // Main table remove action column so it scrolls without the actions
        if (table.getColumnModel().getColumnCount() > actionColumnIndex) {
            table.removeColumn(table.getColumnModel().getColumn(actionColumnIndex));
        }

        // Fixed table keeps only the action column
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

        // Format decimal score columns (indices 6 and 7 in model) to 2 decimal places and right-align
        DecimalCellRenderer decimalRenderer = new DecimalCellRenderer();
        int modelCol6 = 6; // model index
        int modelCol7 = 7;
        // If the table still has those columns, set renderer by view index where applicable
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            int modelIndex = table.getColumnModel().getColumn(i).getModelIndex();
            if (modelIndex == modelCol6 || modelIndex == modelCol7) {
                table.getColumnModel().getColumn(i).setCellRenderer(decimalRenderer);
            }
        }

        JScrollPane mainScroll = new JScrollPane(table);
        JScrollPane fixedScroll = new JScrollPane(fixedActionTable);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        fixedScroll.setBorder(BorderFactory.createEmptyBorder());

        // Synchronize vertical scrollbars
        fixedScroll.getVerticalScrollBar().setModel(mainScroll.getVerticalScrollBar().getModel());

        fixedScroll.setPreferredSize(new Dimension(96, 0));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEtchedBorder());
        wrapper.add(mainScroll, BorderLayout.CENTER);
        wrapper.add(fixedScroll, BorderLayout.EAST);

        JScrollBar sharedHorizontalBar = new JScrollBar(JScrollBar.HORIZONTAL);
        sharedHorizontalBar.setModel(mainScroll.getHorizontalScrollBar().getModel());
        wrapper.add(sharedHorizontalBar, BorderLayout.SOUTH);
        return new JScrollPane(wrapper);
    }

    private static class DecimalCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private final java.text.DecimalFormat fmt = new java.text.DecimalFormat("0.00");

        public DecimalCellRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        protected void setValue(Object value) {
            if (value instanceof Number) {
                setText(fmt.format(((Number) value).doubleValue()));
            } else if (value instanceof String) {
                try {
                    double d = Double.parseDouble(((String) value).replace(',', '.'));
                    setText(fmt.format(d));
                } catch (Exception e) {
                    setText("");
                }
            } else {
                setText("");
            }
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
        searchPanel.add(new JLabel("Loại Giải:"));
        searchPanel.add(txtSearchGiai);
        searchCard.add(searchPanel, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrev = new JButton("◀");
        JButton btnNext = new JButton("▶");
        styleButton(btnPrev, COLOR_BLUE);
        styleButton(btnNext, COLOR_BLUE);

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

    private void applyColumnWidths() {
        int[] widths = {50, 120, 100, 120, 100, 120, 180, 260, 80, 100};
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
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
        txtSearchCccd.getDocument().addDocumentListener(listener);
        txtSearchGiai.getDocument().addDocumentListener(listener);
    }

    private void loadPage(int page) {
        try {
            String cccd = txtSearchCccd.getText();
            String giai = txtSearchGiai.getText();
            totalPages = service.countPages(cccd, giai);
            currentRows = service.getRows(cccd, giai, page);

            tableModel.setRowCount(0);
            for (int i = 0; i < currentRows.size(); i++) {
                UuTienXetTuyenDTO row = currentRows.get(i);
                tableModel.addRow(new Object[]{
                        (page - 1) * PAGE_SIZE + i + 1,
                        row.getTsCccd(),
                        row.getCapQuocGia(),
                        row.getDoiTuyen(),
                        row.getMaMon(),
                        row.getLoaiGiai(),
                        row.getDiemCongMonDatMc(),
                        row.getDiemCongKhongMonDatMc(),
                    normalizeCoChungChi(row.getCoChungChi()),
                        ""
                });
            }

            currentPage = page;
            lblPaging.setText("Trang " + currentPage + "/" + totalPages);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRow() {
        JOptionPane.showMessageDialog(this, "Tính năng đang phát triển", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void importFromExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel ưu tiên xét tuyển");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel files (*.xlsx)", "xlsx"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có muốn xóa dữ liệu Ưu tiên xét tuyển cũ trước khi import không?\nKhuyến nghị: Có (để tránh dữ liệu import sai cũ).",
                    "Xác nhận import",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (!service.deleteAll()) {
                    JOptionPane.showMessageDialog(this, "Không thể xóa dữ liệu cũ trước khi import.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            List<UuTienXetTuyenDTO> imported = UuTienXetTuyenExcelImportUtil.importRows(file);
            if (imported.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu hợp lệ", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int success = 0, fail = 0;
            for (UuTienXetTuyenDTO row : imported) {
                if (service.upsertByKey(row)) {
                    success++;
                } else {
                    fail++;
                }
            }

            logImportResult(file.getName(), success, fail);

            JOptionPane.showMessageDialog(this, "Import: " + success + " thành công, " + fail + " lỗi", "Kết quả", JOptionPane.INFORMATION_MESSAGE);
            loadPage(1);
        } catch (Exception ex) {
            logImportFailure(file.getName(), ex.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logImportResult(String fileName, int success, int fail) {
        try {
            String username = SessionManager.getCurrentUsername();
            if (username == null || username.trim().isEmpty()) {
                username = "unknown";
            }

            AuditLogDTO dto = new AuditLogDTO();
            dto.setUsername(username);
            dto.setAction("IMPORT");
            dto.setModule("PRIORITY_ADMISSION");
            dto.setTableName("xt_uutien_xettuyen");
            dto.setRecordId(fileName);
            dto.setRecordInfo("Import Excel file=" + fileName + ", success=" + success + ", fail=" + fail);
            dto.setStatus(fail == 0 ? "SUCCESS" : "PARTIAL_SUCCESS");
            auditLogService.logAction(dto);
        } catch (Exception ignore) {
        }
    }

    private void logImportFailure(String fileName, String errorMessage) {
        try {
            String username = SessionManager.getCurrentUsername();
            if (username == null || username.trim().isEmpty()) {
                username = "unknown";
            }

            AuditLogDTO dto = new AuditLogDTO();
            dto.setUsername(username);
            dto.setAction("IMPORT");
            dto.setModule("PRIORITY_ADMISSION");
            dto.setTableName("xt_uutien_xettuyen");
            dto.setRecordId(fileName);
            dto.setRecordInfo("Import failed for file=" + fileName);
            dto.setStatus("FAILED");
            dto.setErrorMsg(errorMessage);
            auditLogService.logAction(dto);
        } catch (Exception ignore) {
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
    }

    private void styleTableHeader(JTable tbl) {
        JTableHeader header = tbl.getTableHeader();
        header.setBackground(COLOR_BLUE);
        header.setForeground(Color.WHITE);
    }

    private String normalizeCoChungChi(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.equalsIgnoreCase("Y") || trimmed.equalsIgnoreCase("N")) {
            return trimmed.toUpperCase();
        }
        if (trimmed.equals("1") || trimmed.equals("0")) {
            return trimmed;
        }
        return "";
    }

    // Renderer and editor for action buttons
    private class ActionCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton btnView;
        private final JButton btnDel;

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
            btnView = new JButton();
            btnDel = new JButton();

            // Load icons from resources
            try {
                java.net.URL penUrl = getClass().getResource("/icons/icons8-ball-point-pen-16.png");
                java.net.URL delUrl = getClass().getResource("/icons/icons8-delete-16.png");
                if (penUrl != null) btnView.setIcon(new ImageIcon(penUrl));
                if (delUrl != null) btnDel.setIcon(new ImageIcon(delUrl));
            } catch (Exception ignore) {}

            styleButton(btnView, COLOR_GREEN);
            styleButton(btnDel, COLOR_RED);
            btnView.setMargin(new Insets(4, 6, 4, 6));
            btnDel.setMargin(new Insets(4, 6, 4, 6));
            btnView.setFocusable(false);
            btnDel.setFocusable(false);
            btnView.setToolTipText("Sửa");
            btnDel.setToolTipText("Xóa");
            add(btnView);
            add(btnDel);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        private final JButton btnView = new JButton();
        private final JButton btnDel = new JButton();
        private int editingRow = -1;

        public ActionCellEditor() {
            // load icons
            try {
                java.net.URL penUrl = getClass().getResource("/icons/icons8-ball-point-pen-16.png");
                java.net.URL delUrl = getClass().getResource("/icons/icons8-delete-16.png");
                if (penUrl != null) btnView.setIcon(new ImageIcon(penUrl));
                if (delUrl != null) btnDel.setIcon(new ImageIcon(delUrl));
            } catch (Exception ignore) {}

            styleButton(btnView, COLOR_GREEN);
            styleButton(btnDel, COLOR_RED);
            btnView.setMargin(new Insets(4, 6, 4, 6));
            btnDel.setMargin(new Insets(4, 6, 4, 6));
            btnView.setFocusable(false);
            btnDel.setFocusable(false);
            panel.add(btnView);
            panel.add(btnDel);

            btnView.addActionListener(e -> {
                    if (editingRow >= 0 && editingRow < currentRows.size()) {
                        UuTienXetTuyenDTO dto = currentRows.get(editingRow);
                        JOptionPane.showMessageDialog(UuTienXetTuyenPanel.this,
                            "CCCD: " + dto.getTsCccd() + "\nMã môn: " + dto.getMaMon() + "\nLoại giải: " + dto.getLoaiGiai(),
                            "Chi tiết", JOptionPane.INFORMATION_MESSAGE);
                    }
            });

            btnDel.addActionListener(e -> {
                if (editingRow >= 0 && editingRow < currentRows.size()) {
                    int confirm = JOptionPane.showConfirmDialog(UuTienXetTuyenPanel.this, "Xác nhận xóa dòng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            // If service supports deleteById, call it. Otherwise just refresh UI.
                            UuTienXetTuyenDTO dto = currentRows.get(editingRow);
                            if (dto.getIdUtxt() != null) {
                                try {
                                    service.deleteById(dto.getIdUtxt());
                                } catch (Exception ex) {
                                    // ignore and fall back to UI removal
                                }
                            }
                            currentRows.remove(editingRow);
                            loadPage(1);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(UuTienXetTuyenPanel.this, "Xóa thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.editingRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }
}
