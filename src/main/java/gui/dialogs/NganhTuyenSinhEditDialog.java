package gui.dialogs;

import dto.NganhTuyenSinhDTO;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class NganhTuyenSinhEditDialog extends JDialog {
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtChuongTrinh = new JTextField();
    private final JTextField txtNguong = new JTextField();
    private final JTextField txtChiTieu = new JTextField();

    private NganhTuyenSinhDTO result;

    private NganhTuyenSinhEditDialog(Frame owner, NganhTuyenSinhDTO source, boolean editMode) {
        super(owner, editMode ? "Sửa ngành tuyển sinh" : "Thêm ngành tuyển sinh", true);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        form.add(new JLabel("Mã xét tuyển"));
        form.add(txtMa);

        form.add(new JLabel("Tên ngành"));
        form.add(txtTen);

        form.add(new JLabel("Chương trình đào tạo"));
        form.add(txtChuongTrinh);

        form.add(new JLabel("Ngưỡng đầu vào"));
        form.add(txtNguong);

        form.add(new JLabel("Chỉ tiêu chốt"));
        form.add(txtChiTieu);

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        JPanel actions = new JPanel();
        actions.add(btnSave);
        actions.add(btnCancel);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        if (source != null) {
            txtMa.setText(safe(source.getMaXetTuyen()));
            txtTen.setText(safe(source.getTenNganh()));
            txtChuongTrinh.setText(safe(source.getChuongTrinh()));
            txtNguong.setText(safe(source.getNguongDauVao()));
            txtChiTieu.setText(source.getChiTieuChot() == null ? "" : source.getChiTieuChot().toString());
        }

        btnSave.addActionListener(e -> onSave(source));
        btnCancel.addActionListener(e -> { result = null; dispose(); });

        pack();
        setSize(600, Math.max(getHeight(), 260));
        setLocationRelativeTo(owner);
    }

    private void onSave(NganhTuyenSinhDTO source) {
        String ma = txtMa.getText() == null ? "" : txtMa.getText().trim();
        String ten = txtTen.getText() == null ? "" : txtTen.getText().trim();
        String ct = txtChuongTrinh.getText() == null ? "" : txtChuongTrinh.getText().trim();
        String nguong = txtNguong.getText() == null ? "" : txtNguong.getText().trim();
        String chitieuText = txtChiTieu.getText() == null ? "" : txtChiTieu.getText().trim();

        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã xét tuyển không được để trống.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên ngành không được để trống.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer ctVal = null;
        if (!chitieuText.isEmpty()) {
            try { ctVal = Integer.parseInt(chitieuText); } catch (Exception ex) { /* leave null */ }
        }

        NganhTuyenSinhDTO dto = new NganhTuyenSinhDTO();
        if (source != null) dto.setId(source.getId());
        dto.setMaXetTuyen(ma);
        dto.setTenNganh(ten);
        dto.setChuongTrinh(ct.isEmpty() ? null : ct);
        dto.setNguongDauVao(nguong.isEmpty() ? null : nguong);
        dto.setChiTieuChot(ctVal);

        result = dto;
        dispose();
    }

    public static NganhTuyenSinhDTO showDialog(java.awt.Component parent, NganhTuyenSinhDTO source, boolean editMode) {
        Frame owner = JOptionPane.getFrameForComponent(parent);
        NganhTuyenSinhEditDialog dialog = new NganhTuyenSinhEditDialog(owner, source, editMode);
        dialog.setVisible(true);
        return dialog.result;
    }

    private String safe(String v) { return v == null ? "" : v; }
}
