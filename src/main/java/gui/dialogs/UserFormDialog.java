package gui.dialogs;

import bus.UserService;
import dto.UserDTO;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridLayout;

public class UserFormDialog {

    private final UserDTO base;
    private final boolean isEdit;

    public UserFormDialog(UserDTO user) {
        this.base = user != null ? user : new UserDTO();
        this.isEdit = user != null && user.getId() != null;
    }

 
    public UserDTO show() {
        JTextField tfUsername = new JTextField(emptyIfNull(base.getUsername()), 15);
        JTextField tfFullname = new JTextField(emptyIfNull(base.getFullname()), 15);
        JPasswordField tfPassword = new JPasswordField(15);
        JPasswordField tfPasswordConfirm = new JPasswordField(15);

        JComboBox<String> cbRole = new JComboBox<>(new String[] { "ADMIN", "NHAN_VIEN" });
        if (base.getRole() != null) {
            cbRole.setSelectedItem(base.getRole());
        } else {
            cbRole.setSelectedItem("NHAN_VIEN");
        }

        JComboBox<String> cbStatus = new JComboBox<>(new String[] { "Hoạt động", "Khóa" });
        cbStatus.setSelectedItem(base.isActive() ? "Hoạt động" : "Khóa");

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));

        form.add(new JLabel("Username:"));
        if (isEdit) {
            tfUsername.setEditable(false);
        }
        form.add(tfUsername);

        form.add(new JLabel("Họ tên:"));
        form.add(tfFullname);

        if (!isEdit) {
            form.add(new JLabel("Mật khẩu:"));
            form.add(tfPassword);

            form.add(new JLabel("Xác nhận mật khẩu:"));
            form.add(tfPasswordConfirm);
        }

        form.add(new JLabel("Vai trò:"));
        form.add(cbRole);

        form.add(new JLabel("Trạng thái:"));
        form.add(cbStatus);

        int result = JOptionPane.showConfirmDialog(
                null,
                form,
                isEdit ? "Sửa user" : "Thêm user mới",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        // Validation
        String username = tfUsername.getText().trim();
        String fullname = tfFullname.getText().trim();
        String password = new String(tfPassword.getPassword());
        String passwordConfirm = new String(tfPasswordConfirm.getPassword());
        String role = (String) cbRole.getSelectedItem();
        int status = "Hoạt động".equals(cbStatus.getSelectedItem()) ? 1 : 0;

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập username", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (fullname.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập họ tên", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (!isEdit) {
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Vui lòng nhập mật khẩu", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            if (!password.equals(passwordConfirm)) {
                JOptionPane.showMessageDialog(null, "Mật khẩu không khớp", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(null, "Mật khẩu phải tối thiểu 6 ký tự", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }


        UserDTO dto = new UserDTO();
        if (isEdit) {
            dto.setId(base.getId());
        }
        dto.setUsername(username);
        dto.setFullname(fullname);
        if (!isEdit) {
            dto.setPassword(password);
        }
        dto.setRole(role);
        dto.setStatus(status);

        return dto;
    }

    private String emptyIfNull(Object value) {
        return value != null ? value.toString() : "";
    }
}
