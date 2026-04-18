package gui.panels;

import bus.UserService;
import dto.UserDTO;
import gui.dialogs.UserFormDialog;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private final UserService userService;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private final Color SIDEBAR_PURPLE = new Color(87, 82, 174);
    private final Color MAIN_BG = new Color(223, 234, 252);
    private final Color SELECTION_BG = new Color(200, 215, 245);

    // Tải Icon an toàn
    private final ImageIcon editIcon = getIcon("/icons/edit.png");
    private final ImageIcon lockIcon = getIcon("/icons/icons8-lock-16.png");
    private final ImageIcon deleteIcon = getIcon("/icons/icons8-delete-16.png");
    private final ImageIcon addIcon = getIcon("/icons/add.png");

    // NEW: Search and pagination
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnPrevious;
    private JButton btnNext;
    private JLabel lblPageInfo;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearchKeyword = "";

    public UserManagementPanel() {
        this.userService = new UserService();
        this.tableModel = new DefaultTableModel(new String[] {
                "ID", "Username", "Họ tên", "Vai trò", "Trạng thái", "Thao tác"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 5;
            }
        };
        this.table = new JTable(tableModel);
        setupPanel();
        setupActionColumn();
        loadUsers();
    }

    private void setupPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Quản lý tài khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JButton btnAdd = new JButton(" Thêm người dùng", addIcon);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setBackground(SIDEBAR_PURPLE);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setPreferredSize(new Dimension(190, 40));
        btnAdd.addActionListener(e -> handleAddUser());

        header.add(title, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm kiếm:"));
        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleRealtimeSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleRealtimeSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleRealtimeSearch();
            }
        });
        searchPanel.add(txtSearch);
        btnSearch = new JButton("Tìm");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setBackground(SIDEBAR_PURPLE);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> handleSearch());
        searchPanel.add(btnSearch);

        table.setRowHeight(45);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(Color.BLACK);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 247, 250));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer centerBoldRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)));

                if (s) {
                    comp.setFont(new Font("Segoe UI", Font.BOLD, 14));
                } else {
                    comp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                }
                return comp;
            }
        };

        for (int i = 0; i < 5; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerBoldRenderer);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 240)));
        sp.getViewport().setBackground(Color.WHITE);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(sp, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        paginationPanel.setOpaque(false);

        btnPrevious = new JButton("Trước");
        btnPrevious.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPrevious.setBackground(SIDEBAR_PURPLE);
        btnPrevious.setForeground(Color.WHITE);
        btnPrevious.setFocusPainted(false);
        btnPrevious.addActionListener(e -> handlePreviousPage());

        lblPageInfo = new JLabel("Trang 1 / 1");
        lblPageInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));

        btnNext = new JButton("Sau");
        btnNext.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnNext.setBackground(SIDEBAR_PURPLE);
        btnNext.setForeground(Color.WHITE);
        btnNext.setFocusPainted(false);
        btnNext.addActionListener(e -> handleNextPage());

        paginationPanel.add(btnPrevious);
        paginationPanel.add(lblPageInfo);
        paginationPanel.add(btnNext);

        add(paginationPanel, BorderLayout.SOUTH);
    }

    private void setupActionColumn() {
        TableColumn col = table.getColumnModel().getColumn(5);
        col.setCellRenderer(new ActionButtonRenderer());
        col.setCellEditor(new ActionButtonEditor());
        col.setPreferredWidth(150);
    }

    private void loadUsers() {
        try {
            tableModel.setRowCount(0);
            List<UserDTO> users;

            if (currentSearchKeyword.isEmpty()) {
                users = userService.getUsersByPage(currentPage, UserService.PAGE_SIZE);
                totalPages = userService.countPages();
            } else {
                users = userService.searchUsers(currentSearchKeyword);
                totalPages = 1; // Search results on single page
            }

            for (UserDTO user : users) {
                tableModel.addRow(new Object[] {
                        user.getId(), user.getUsername(), user.getFullname(),
                        userService.getRoleDisplay(user.getRole()), user.getStatusDisplay(), ""
                });
            }

            updatePaginationControls();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // --- Inner Classes for Actions ---
    class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setBackground(s ? SELECTION_BG : Color.WHITE);
            removeAll();
            add(new JLabel(editIcon));
            add(new JLabel("|"));
            add(new JLabel(lockIcon));
            add(new JLabel("|"));
            add(new JLabel(deleteIcon));
            return this;
        }
    }

    class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        private final JButton bEdit = createIconButton(editIcon);
        private final JButton bLock = createIconButton(lockIcon);
        private final JButton bDelete = createIconButton(deleteIcon);
        private Integer selectedId;

        public ActionButtonEditor() {
            bEdit.addActionListener(e -> {
                stopCellEditing();
                handleEditUser(selectedId);
            });
            bLock.addActionListener(e -> {
                stopCellEditing();
                handleToggleUserStatus(selectedId);
            });
            bDelete.addActionListener(e -> {
                stopCellEditing();
                handleDeleteUser(selectedId);
            });
            p.add(bEdit);
            p.add(bLock);
            p.add(bDelete);
        }

        private JButton createIconButton(ImageIcon icon) {
            JButton btn = new JButton(icon);
            btn.setPreferredSize(new Dimension(28, 28));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            selectedId = (Integer) t.getValueAt(r, 0);
            p.setBackground(SELECTION_BG);
            return p;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    // --- Helper Methods ---
    private ImageIcon getIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        return (url != null) ? new ImageIcon(url) : null;
    }

    private void handleAddUser() {
        UserFormDialog dialog = new UserFormDialog(null);
        UserDTO result = dialog.show();
        if (result != null) {
            try {
                if (userService.createUser(result)) {
                    JOptionPane.showMessageDialog(this, "Thêm thành công!");
                    loadUsers();
                }
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        }
    }

    public void handleEditUser(Integer id) {
        try {
            UserDTO u = userService.getUserById(id);
            if (u != null) {
                UserFormDialog d = new UserFormDialog(u);
                if (d.show() != null && userService.updateUser(u)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    loadUsers();
                }
            }
        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }

    public void handleToggleUserStatus(Integer id) {
        try {
            if (userService.toggleUserStatus(id)) {
                JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thành công!");
                loadUsers();
            }
        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }

    public void handleDeleteUser(Integer id) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa tài khoản này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (userService.deleteUser(id)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                    loadUsers();
                }
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, "Lỗi: " + msg, "Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void handleRealtimeSearch() {
        currentSearchKeyword = txtSearch.getText().trim();
        currentPage = 1; // Reset to first page
        loadUsers();
    }

    private void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadUsers();
        }
    }

    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadUsers();
        }
    }


    private void updatePaginationControls() {
        btnPrevious.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
    }
    
    private void handleSearch() {
    String keyword = ""; 
    System.out.println("Đang tìm kiếm với từ khóa: " + keyword);
    loadUsers(); 
}
}