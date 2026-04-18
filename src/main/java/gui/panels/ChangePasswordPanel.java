package gui.panels;

import bus.AuthService;
import com.formdev.flatlaf.FlatClientProperties;
import dto.UserDTO;
import gui.MainFrame;
import gui.SessionManager; // Cần import để lấy user hiện tại
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ChangePasswordPanel extends JPanel {

    private final AuthService authService;
    private final MainFrame parentFrame;

    private final JPasswordField txtCurrentPassword;
    private final JPasswordField txtNewPassword;
    private final JPasswordField txtConfirmPassword;

    private final Color SIDEBAR_PURPLE = new Color(87, 82, 174);
    private final Color MAIN_BG = new Color(223, 234, 252);

    public ChangePasswordPanel(MainFrame parentFrame) {
        this.authService = new AuthService();
        // SỬA LỖI TẠI ĐÂY: Gán trực tiếp, không dùng 'new MainFrame()'
        this.parentFrame = parentFrame;

        this.txtCurrentPassword = new JPasswordField();
        this.txtNewPassword = new JPasswordField();
        this.txtConfirmPassword = new JPasswordField();

        // Thêm style hiện đại cho các ô nhập liệu
        stylePasswordField(txtCurrentPassword, "Mật khẩu hiện tại");
        stylePasswordField(txtNewPassword, "Mật khẩu mới");
        stylePasswordField(txtConfirmPassword, "Xác nhận mật khẩu mới");

        setupPanel();
    }

    private void stylePasswordField(JPasswordField field, String placeholder) {
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 12; showRevealButton: true");
        field.setPreferredSize(new Dimension(300, 40));
    }

    private void setupPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(MAIN_BG);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Bảo mật tài khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        JLabel subtitle = new JLabel("Thay đổi mật khẩu định kỳ để bảo vệ tài khoản của bạn");
        subtitle.setForeground(new Color(100, 116, 139));
        
        Box vBoxHead = Box.createVerticalBox();
        vBoxHead.add(title);
        vBoxHead.add(subtitle);
        header.add(vBoxHead, BorderLayout.WEST);

        // Form panel (Dùng Box để căn giữa form cho đẹp)
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        // Bo góc cho form trắng
        formPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 20");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Vẽ các dòng input
        addFormRow(formPanel, "Mật khẩu cũ:", txtCurrentPassword, gbc, 0);
        addFormRow(formPanel, "Mật khẩu mới:", txtNewPassword, gbc, 1);
        addFormRow(formPanel, "Xác nhận:", txtConfirmPassword, gbc, 2);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnChange = new JButton("Cập nhật mật khẩu");
        btnChange.setBackground(SIDEBAR_PURPLE);
        btnChange.setForeground(Color.WHITE);
        btnChange.setPreferredSize(new Dimension(160, 40));
        btnChange.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChange.addActionListener(e -> handleChangePassword());

        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> clearForm());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnChange);

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(buttonPanel, gbc);

        centerWrapper.add(formPanel);

        add(header, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel p, String label, JComponent comp, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(comp, gbc);
    }

    private void handleChangePassword() {
        String currentPass = new String(txtCurrentPassword.getPassword()).trim();
        String newPass = new String(txtNewPassword.getPassword()).trim();
        String confirmPass = new String(txtConfirmPassword.getPassword()).trim();

        // 1. Kiểm tra rỗng
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Kiểm tra khớp mật khẩu
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Lấy User hiện tại từ Session (SỬA LỖI QUAN TRỌNG)
        UserDTO currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Phiên đăng nhập hết hạn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Gọi AuthService xử lý (nhận vào id, pass cũ, pass mới)
            if (authService.changePassword(currentUser.getId(), currentPass, newPass)) {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không chính xác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtCurrentPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
    }
}