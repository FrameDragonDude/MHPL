package gui.panels;

import bus.UuTienXetTuyenService;
import dto.UuTienXetTuyenDTO;
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
            "STT", "CCCD", "Cấp", "Đội Tuyển", "Mã Môn", "Loại Giải", "Điểm Mon Đặt", "Điểm Khác", "C/C", "Thao tác"
    };

    private final UuTienXetTuyenService service;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField txtSearchCccd;
    private final JTextField txtSearchGiai;
    private final JLabel lblPaging;
    private final int actionColumnIndex;
    private int currentPage = 1;
    private int totalPages = 1;
    private List<UuTienXetTuyenDTO> currentRows = new ArrayList<>();

    public UuTienXetTuyenPanel() {
        this.service = new UuTienXetTuyenService();
        this.tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == actionColumnIndex;
            }
        };
        this.table = new JTable(tableModel);
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
        applyColumnWidths();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEtchedBorder());
        return scroll;
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
        int[] widths = {50, 120, 100, 120, 100, 100, 100, 100, 50, 80};
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
                        row.getCoChungChi(),
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

            JOptionPane.showMessageDialog(this, "Import: " + success + " thành công, " + fail + " lỗi", "Kết quả", JOptionPane.INFORMATION_MESSAGE);
            loadPage(1);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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
}
