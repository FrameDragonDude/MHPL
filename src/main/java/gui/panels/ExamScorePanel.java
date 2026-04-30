package gui.panels;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import utils.excel.ExamScoresExcelImportUtil;
import utils.excel.ExamScoresExcelExportUtil;
import bus.ExamScoreService;
import dto.ExamScoreDTO;
import gui.MainFrame;
import gui.dialogs.ExamScoreFormDialog;
import lucee.debug.Main;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExamScorePanel extends JPanel {
    private static final Color COLOR_GREEN = new Color(46, 125, 50);
    private static final Color COLOR_BLUE = new Color(21, 101, 192);
    private static final Color COLOR_RED = new Color(198, 40, 40);

    // Định nghĩa các cột dựa trên Table xt_diemthixettuyen
    private static final String[] TABLE_COLUMNS = {
            "STT", "ID", "CCCD", "SBD", "Phương thức", "Toán", "Lý", "Hóa", "Sinh", 
            "Sử", "Địa", "Văn", "N1 Thi", "N1 CC", "CNCN", "CNNN", "Tin", 
            "KTPL", "NL1", "NK1", "NK2", "Thao tác"
    };

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTable fixedActionTable;
    private final JTextField txtSearchCccd;
    private final JComboBox<String> cbFilterPhuongThuc;
    private final JLabel lblPaging;
    private final int actionColumnIndex;
    private final ExamScoreService examScoreService;
    private int currentPage = 1;
    private int totalPages = 1;
    private MainFrame mainFrame;

    public ExamScorePanel (MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.examScoreService = new ExamScoreService();
        this.actionColumnIndex = TABLE_COLUMNS.length - 1;

        this.tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == actionColumnIndex;
            }
        };

        this.table = new JTable(tableModel);
        this.fixedActionTable = new JTable(tableModel);
        this.txtSearchCccd = new JTextField(20);
        this.cbFilterPhuongThuc = new JComboBox<>(new String[]{
            "Tất cả", "THPT", "VSAT", "ĐGNL"
        });
        this.lblPaging = new JLabel("Trang 1/1 (20 dòng/trang)");

        setupUI();
        attachLiveSearch();
        refreshData();
    }

    private void setupUI() {
        setLayout(new BorderLayout(8, 8));
        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTopPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        
        JLabel title = new JLabel("Quản lý điểm thi xét tuyển");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        
        JLabel subtitle = new JLabel("Tra cứu và cập nhật điểm thi thí sinh");
        subtitle.setForeground(new Color(75, 85, 99));

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnImport = new JButton("Nhập Excel");
        JButton btnExport = new JButton("Xuất Excel");
        JButton btnAdd = new JButton("+ Nhập điểm");
        JButton btnStats = new JButton("Xem thống kê");
        styleButton(btnStats, new Color(255, 178, 102));
        styleButton(btnImport, COLOR_GREEN);
        styleButton(btnExport, new Color(30, 136, 229));
        styleButton(btnAdd, COLOR_BLUE);

        btnExport.addActionListener(e -> exportExamScoresToExcel());

        btnImport.addActionListener(e -> handleImportExcel());
        
        btnStats.addActionListener(e -> {
            mainFrame.switchPanel("STATISTICS");
        });

        btnAdd.addActionListener(e -> {
            ExamScoreFormDialog dialog = new ExamScoreFormDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                boolean success = examScoreService.addExamScore(dialog.getResult());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Thêm điểm thi thành công!");
                    refreshData();
                } else {    
                    JOptionPane.showMessageDialog(this, "Thêm điểm thi thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        actions.add(btnStats);
        actions.add(btnExport);
        actions.add(btnImport);
        actions.add(btnAdd);

        wrapper.add(left, BorderLayout.WEST);
        wrapper.add(actions, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel buildTablePanel() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(34);
        styleTableHeader(table);

        fixedActionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        fixedActionTable.setRowHeight(34);
        styleTableHeader(fixedActionTable);

        applyColumnWidths();

        if (table.getColumnCount() > actionColumnIndex) {
            table.removeColumn(table.getColumnModel().getColumn(actionColumnIndex));
        }

        for (int i = fixedActionTable.getColumnModel().getColumnCount() - 1; i >= 0; i--) {
            if (fixedActionTable.getColumnModel().getColumn(i).getModelIndex() != actionColumnIndex) {
                fixedActionTable.removeColumn(fixedActionTable.getColumnModel().getColumn(i));
            }
        }

        fixedActionTable.getColumnModel().getColumn(0).setCellRenderer(new ActionCellRenderer());
        fixedActionTable.getColumnModel().getColumn(0).setCellEditor(new ActionCellEditor());
        fixedActionTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        fixedActionTable.setSelectionModel(table.getSelectionModel());

        JScrollPane mainScroll = new JScrollPane(table);
        JScrollPane fixedScroll = new JScrollPane(fixedActionTable);
        
        mainScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        fixedScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        fixedScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        fixedScroll.setBorder(BorderFactory.createEmptyBorder());
        fixedScroll.setViewportBorder(BorderFactory.createEmptyBorder());
        fixedScroll.getVerticalScrollBar().setModel(mainScroll.getVerticalScrollBar().getModel());
        fixedScroll.setPreferredSize(new Dimension(100, 0));
        JScrollBar sharedHorizontalBar = new JScrollBar(JScrollBar.HORIZONTAL);
        sharedHorizontalBar.setModel(mainScroll.getHorizontalScrollBar().getModel());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEtchedBorder());
        wrapper.add(mainScroll, BorderLayout.CENTER);
        wrapper.add(fixedScroll, BorderLayout.EAST);
        wrapper.add(sharedHorizontalBar, BorderLayout.SOUTH);

        return wrapper;
    }

    private void applyColumnWidths() {
        int[] widths = {50, 50, 120, 120, 100, 60, 60, 60, 60, 60, 60, 60, 70, 70, 70, 70, 60, 70, 70, 70, 70, 100};
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private JPanel buildBottomPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));

        JPanel searchCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchCard.setBackground(Color.WHITE);
        searchCard.setBorder(BorderFactory.createLineBorder(new Color(227, 231, 239)));
        
        searchCard.add(new JLabel("Tìm kiếm CCCD/SBD:"));
        searchCard.add(txtSearchCccd);

        searchCard.add(new JLabel("Lọc theo phương thức: "));
        searchCard.add(cbFilterPhuongThuc);
        
        JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pagingPanel.add(lblPaging);
        JButton btnPrev = new JButton("◀");
        JButton btnNext = new JButton("▶");
        styleButton(btnPrev, COLOR_BLUE);
        styleButton(btnNext, COLOR_BLUE);

        cbFilterPhuongThuc.addActionListener(e -> {
            currentPage = 1;
            refreshData();
        });

        btnPrev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                refreshData();
            }
        });
        btnNext.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                refreshData();
            }
        });

        pagingPanel.add(btnPrev);
        pagingPanel.add(btnNext);

        wrapper.add(searchCard, BorderLayout.CENTER);
        wrapper.add(pagingPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private void styleButton(JButton button, Color bg) {
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setMargin(new Insets(5, 15, 5, 15));
    }

    private void styleTableHeader(JTable t) {
        JTableHeader header = t.getTableHeader();
        header.setBackground(COLOR_BLUE);
        header.setForeground(Color.WHITE);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
    }

    private void refreshData() {
        String keyword = txtSearchCccd.getText().trim();
        String filter = cbFilterPhuongThuc.getSelectedItem().toString();
        if (filter.equals("Tất cả")) {
            filter = null;
        }
        try {
            this.totalPages = examScoreService.countPages(keyword, filter);
            int totalRows = examScoreService.countRows(keyword, filter);
            lblPaging.setText(String.format("Trang %d/%d (Tổng %d dòng)", currentPage, totalPages, totalRows));

            List<ExamScoreDTO> list = examScoreService.getExamScores(keyword, filter, currentPage);
            tableModel.setRowCount(0);
            int stt = (currentPage - 1) * ExamScoreService.PAGE_SIZE + 1;

            for (ExamScoreDTO d : list) {
                tableModel.addRow(new Object[]{
                        stt++,
                        d.getIdDiemThi(),
                        d.getCccd(),
                        d.getSoBaoDanh(),
                        d.getPhuongThuc(),
                        d.getDiemTo(),
                        d.getDiemLi(),
                        d.getDiemHo(),
                        d.getDiemSi(),
                        d.getDiemSu(),
                        d.getDiemDi(),
                        d.getDiemVa(),
                        d.getDiemN1Thi(),
                        d.getDiemN1Cc(),
                        d.getDiemCncn(),
                        d.getDiemCnnn(),
                        d.getDiemTi(),
                        d.getDiemKtpl(),
                        d.getDiemNl1(),
                        d.getDiemNk1(),
                        d.getDiemNk2(),
                        "" // Cột thao tác
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attachLiveSearch() {
        txtSearchCccd.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { 
                currentPage = 1;
                refreshData(); 
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { 
                currentPage = 1;
                refreshData(); 
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { 
                currentPage = 1;
                refreshData(); 
            }
        });
    }

    private void handleImportExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file Excel");
        
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fileChooser.getSelectedFile();

        new Thread(() -> {
            try {
                List<ExamScoreDTO> list = ExamScoresExcelImportUtil.importExamScores(file);

                int success = examScoreService.importBatch(list);

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Import thành công " + success + " dòng!");
                    refreshData();
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Lỗi import: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void exportExamScoresToExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Excel điểm thi");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel files (*.xlsx)", "xlsx"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new java.io.File(file.getParentFile(), file.getName() + ".xlsx");
        }

        try {
            // 👉 Lấy toàn bộ dữ liệu theo filter hiện tại
            String keyword = txtSearchCccd.getText().trim();
            String filter = cbFilterPhuongThuc.getSelectedItem().toString();
            if (filter.equals("Tất cả")) filter = null;

            List<ExamScoreDTO> list = examScoreService.getAllExamScores(keyword, filter);

            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để export.");
                return;
            }

            ExamScoresExcelExportUtil.exportExamScores(file, list);

            JOptionPane.showMessageDialog(this,
                    "Export thành công " + list.size() + " dòng\nFile: " + file.getAbsolutePath());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi export: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ActionCellRenderer extends JPanel implements TableCellRenderer {
        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
            JButton btnEdit = new JButton("✎");
            JButton btnDelete = new JButton("🗑");
            styleButton(btnEdit, COLOR_GREEN);
            styleButton(btnDelete, COLOR_RED);
            btnEdit.setMargin(new Insets(2, 5, 2, 5));
            btnDelete.setMargin(new Insets(2, 5, 2, 5));
            add(btnEdit);
            add(btnDelete);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            setBackground(isS ? t.getSelectionBackground() : t.getBackground());
            return this;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        public ActionCellEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
            JButton btnEdit = new JButton("✎");
            JButton btnDelete = new JButton("🗑");
            styleButton(btnEdit, COLOR_GREEN);
            styleButton(btnDelete, COLOR_RED);
            btnEdit.setMargin(new Insets(2, 5, 2, 5));
            btnDelete.setMargin(new Insets(2, 5, 2, 5));
            
            btnEdit.addActionListener(e -> {
                stopCellEditing();
                int row = table.getSelectedRow();
                ExamScoreDTO selected = examScoreService.getExamScoreById(Integer.parseInt(tableModel.getValueAt(row, 1).toString()));
                ExamScoreFormDialog dialog = new ExamScoreFormDialog((Frame) SwingUtilities.getWindowAncestor(ExamScorePanel.this), selected);
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    boolean success = examScoreService.updateExamScore(dialog.getResult());
                    if (success) {  
                        JOptionPane.showMessageDialog(btnEdit, "Cập nhật điểm thi thành công!");
                    } else {
                        JOptionPane.showMessageDialog(btnEdit, "Cập nhật điểm thi thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                    refreshData();
                }
            });
            
            btnDelete.addActionListener(e -> {
                stopCellEditing();
                int result = JOptionPane.showConfirmDialog(btnDelete, "Bạn có chắc chắn muốn xóa điểm thi này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    boolean success = examScoreService.deleteExamScore(Integer.parseInt(tableModel.getValueAt(table.getSelectedRow(), 1).toString()));
                    if (success) {
                        JOptionPane.showMessageDialog(btnDelete, "Xóa điểm thi thành công!");
                    } else {
                        JOptionPane.showMessageDialog(btnDelete, "Xóa điểm thi thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                    refreshData();
                }
            });

            panel.add(btnEdit);
            panel.add(btnDelete);
        }
        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean isS, int r, int c) {
            panel.setBackground(t.getSelectionBackground());
            return panel;
        }
        @Override
        public Object getCellEditorValue() { return ""; }
    }
}