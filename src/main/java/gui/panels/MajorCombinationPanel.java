package gui.panels;

import bus.MajorCombinationService;
import dto.MajorCombinationDTO;
import gui.dialogs.MajorCombinationEditDialog;
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
import utils.excel.MajorCombinationExcelImportUtil;

public class MajorCombinationPanel extends JPanel {
    private static final Color COLOR_GREEN = new Color(46, 125, 50);
    private static final Color COLOR_BLUE = new Color(21, 101, 192);
    private static final Color COLOR_RED = new Color(198, 40, 40);

    private static final String[] TABLE_COLUMNS = {
        "STT", "Mã ngành", "Tên ngành", "Mã tổ hợp", "Môn 1", "Môn 2", "Môn 3", "Gốc", "Độ lệch", "Thao tác"
    };

    private static final int COL_STT = 0;
    private static final int COL_MA_NGANH = 1;
    private static final int COL_TEN_NGANH = 2;
    private static final int COL_MA_TO_HOP = 3;
    private static final int COL_MON1 = 4;
    private static final int COL_MON2 = 5;
    private static final int COL_MON3 = 6;
    private static final int COL_GOC = 7;
    private static final int COL_DO_LECH = 8;

    private final MajorCombinationService service;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTable fixedActionTable;

    private final JTextField txtSearchMajor;
    private final JTextField txtSearchToHop;
    private final JLabel lblPaging;
    private final JLabel lblRows;

    private final int actionColumnIndex;
    private int currentPage = 1;
    private int totalPages = 1;
    private List<MajorCombinationDTO> currentRows = new ArrayList<>();

    public MajorCombinationPanel() {
        this.service = new MajorCombinationService();
        this.tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == actionColumnIndex;
            }
        };

        this.table = new JTable(tableModel);
        this.fixedActionTable = new JTable(tableModel);
        this.actionColumnIndex = tableModel.getColumnCount() - 1;

        this.txtSearchMajor = new JTextField(20);
        this.txtSearchToHop = new JTextField(16);
        this.lblPaging = new JLabel("Trang 1/1 (20 dòng/trang)");
        this.lblRows = new JLabel("Tổng dòng: 0");

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

        JLabel title = new JLabel("Quản lý ngành - tổ hợp");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 28f));
        title.setForeground(new Color(17, 24, 39));

        JLabel subtitle = new JLabel("Bảng mapping ngành chuẩn với tổ hợp xét tuyển");
        subtitle.setFont(subtitle.getFont().deriveFont(java.awt.Font.PLAIN, 13f));
        subtitle.setForeground(new Color(75, 85, 99));

        left.add(title);
        left.add(javax.swing.Box.createVerticalStrut(4));
        left.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnImport = new JButton("Import");
        JButton btnAdd = new JButton("+ Thêm dòng");
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
        searchPanel.add(new JLabel("Mã ngành / Tên ngành:"));
        searchPanel.add(txtSearchMajor);
        searchPanel.add(new JLabel("Mã tổ hợp:"));
        searchPanel.add(txtSearchToHop);
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
        int[] widths = {70, 110, 260, 110, 140, 140, 140, 90, 90, 96};
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

        txtSearchMajor.getDocument().addDocumentListener(listener);
        txtSearchToHop.getDocument().addDocumentListener(listener);
    }

    private void loadPage(int page) {
        try {
            String major = txtSearchMajor.getText();
            String toHop = txtSearchToHop.getText();

            int countedPages = service.countPages(major, toHop);
            int safePage = Math.max(1, Math.min(page, countedPages));

            List<MajorCombinationDTO> rows = service.getRows(major, toHop, safePage);
            int totalRows = service.countRows(major, toHop);
            currentRows = new ArrayList<>(rows);

            tableModel.setRowCount(0);
            for (int i = 0; i < rows.size(); i++) {
                MajorCombinationDTO row = rows.get(i);
                int stt = (safePage - 1) * MajorCombinationService.PAGE_SIZE + i + 1;
                tableModel.addRow(new Object[]{
                    stt,
                    emptyIfNull(row.getManganh()),
                    emptyIfNull(row.getTenNganhChuan()),
                    emptyIfNull(row.getMaToHop()),
                    emptyIfNull(row.getMon1()),
                    emptyIfNull(row.getMon2()),
                    emptyIfNull(row.getMon3()),
                    emptyIfNull(row.getGoc()),
                    formatNumber(row.getDoLech()),
                    ""
                });
            }

            this.currentPage = safePage;
            this.totalPages = countedPages;
            this.lblPaging.setText("Trang " + currentPage + "/" + totalPages + " (20 dòng/trang)");
            this.lblRows.setText("Tổng dòng: " + totalRows);
        } catch (SQLException ex) {
            showError("Không thể tải danh sách ngành - tổ hợp", ex);
        }
    }

    private void addRow() {
        MajorCombinationDTO draft = new MajorCombinationDTO();
        MajorCombinationDTO dto = MajorCombinationEditDialog.showDialog(this, draft, false);
        if (dto == null) {
            return;
        }

        try {
            boolean created = service.create(dto);
            if (created) {
                JOptionPane.showMessageDialog(this, "Thêm dòng thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadPage(Integer.MAX_VALUE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Thêm thất bại. Kiểm tra Mã ngành, Mã tổ hợp và 3 môn không được để trống.",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            showError("Không thể thêm dữ liệu", ex);
        }
    }

    private void editRow(int rowIndex) {
        MajorCombinationDTO current = getCurrentRow(rowIndex);
        if (current == null) {
            return;
        }

        MajorCombinationDTO edited = MajorCombinationEditDialog.showDialog(this, current, true);
        if (edited == null) {
            return;
        }
        edited.setId(current.getId());
        edited.setManganh(current.getManganh());

        try {
            boolean updated = service.update(edited);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadPage(currentPage);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Cập nhật thất bại. Kiểm tra Mã ngành, Mã tổ hợp và 3 môn.",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            showError("Không thể cập nhật dữ liệu", ex);
        }
    }

    private void deleteRow(int rowIndex) {
        MajorCombinationDTO current = getCurrentRow(rowIndex);
        if (current == null || current.getId() == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa dòng đang chọn?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

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
            showError("Không thể xóa dữ liệu", ex);
        }
    }

    private void importFromExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel ngành - tổ hợp");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel files (*.xlsx)", "xlsx"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            List<MajorCombinationDTO> imported = MajorCombinationExcelImportUtil.importRows(file);
            if (imported.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu hợp lệ trong file.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int success = 0;
            int fail = 0;
            String firstError = null;

            for (MajorCombinationDTO row : imported) {
                try {
                    if (service.upsertByTbKeys(row)) {
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

    private MajorCombinationDTO getCurrentRow(int tableRow) {
        if (tableRow < 0 || tableRow >= currentRows.size()) {
            return null;
        }
        return currentRows.get(tableRow);
    }

    private void showError(String title, Exception ex) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                this,
                title + ": " + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        ));
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private String formatNumber(Double value) {
        if (value == null) {
            return "";
        }
        return String.format("%.2f", value);
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
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}
