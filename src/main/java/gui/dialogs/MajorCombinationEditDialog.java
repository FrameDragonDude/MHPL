package gui.dialogs;

import dto.MajorCombinationDTO;
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

public final class MajorCombinationEditDialog extends JDialog {
    private final JTextField txtTenNganh = new JTextField();
    private final JTextField txtMaToHop = new JTextField();
    private final JTextField txtMon1 = new JTextField();
    private final JTextField txtMon2 = new JTextField();
    private final JTextField txtMon3 = new JTextField();
    private final JTextField txtGoc = new JTextField();
    private final JTextField txtDoLech = new JTextField();

    private MajorCombinationDTO result;

    private MajorCombinationEditDialog(Frame owner, MajorCombinationDTO source, boolean editMode) {
        super(owner, editMode ? "Sửa ngành - tổ hợp" : "Thêm ngành - tổ hợp", true);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));

        form.add(new JLabel("TEN_NGANHCHUAN"));
        form.add(txtTenNganh);

        form.add(new JLabel("MA_TO_HOP"));
        form.add(txtMaToHop);

        form.add(new JLabel("Môn 1"));
        form.add(txtMon1);

        form.add(new JLabel("Môn 2"));
        form.add(txtMon2);

        form.add(new JLabel("Môn 3"));
        form.add(txtMon3);

        form.add(new JLabel("Gốc"));
        form.add(txtGoc);

        form.add(new JLabel("Độ lệch"));
        form.add(txtDoLech);

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        JPanel actions = new JPanel();
        actions.add(btnSave);
        actions.add(btnCancel);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        if (source != null) {
            txtTenNganh.setText(safe(source.getTenNganhChuan()));
            txtMaToHop.setText(safe(source.getMaToHop()));
            txtMon1.setText(safe(source.getMon1()));
            txtMon2.setText(safe(source.getMon2()));
            txtMon3.setText(safe(source.getMon3()));
            txtGoc.setText(safe(source.getGoc()));
            txtDoLech.setText(source.getDoLech() == null ? "" : String.valueOf(source.getDoLech()));
        }

        btnSave.addActionListener(e -> onSave(source));
        btnCancel.addActionListener(e -> {
            result = null;
            dispose();
        });

        pack();
        setSize(560, Math.max(getHeight(), 300));
        setLocationRelativeTo(owner);
    }

    private void onSave(MajorCombinationDTO source) {
        String tenNganh = txtTenNganh.getText() == null ? "" : txtTenNganh.getText().trim();
        String maToHop = txtMaToHop.getText() == null ? "" : txtMaToHop.getText().trim();
        if (tenNganh.isEmpty()) {
            JOptionPane.showMessageDialog(this, "TEN_NGANHCHUAN không được để trống.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (maToHop.isEmpty()) {
            JOptionPane.showMessageDialog(this, "MA_TO_HOP không được để trống.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String mon1 = txtMon1.getText() == null ? "" : txtMon1.getText().trim();
        String mon2 = txtMon2.getText() == null ? "" : txtMon2.getText().trim();
        String mon3 = txtMon3.getText() == null ? "" : txtMon3.getText().trim();
        if (mon1.isEmpty() || mon2.isEmpty() || mon3.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ba môn không được để trống.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Double doLech;
        String doLechRaw = txtDoLech.getText() == null ? "" : txtDoLech.getText().trim();
        if (doLechRaw.isEmpty()) {
            doLech = 0.0;
        } else {
            try {
                doLech = Double.parseDouble(doLechRaw.replace(',', '.'));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Độ lệch phải là số.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        MajorCombinationDTO dto = new MajorCombinationDTO();
        if (source != null) {
            dto.setId(source.getId());
            dto.setManganh(source.getManganh());
        }
        dto.setTenNganhChuan(tenNganh);
        dto.setMaToHop(maToHop);
        dto.setMon1(mon1);
        dto.setMon2(mon2);
        dto.setMon3(mon3);
        dto.setTbKeys(trimToNull((source != null && source.getTbKeys() != null) ? source.getTbKeys() : null));
        dto.setTenToHop(String.join(", ", mon1, mon2, mon3));
        dto.setGoc(trimToNull(txtGoc.getText()));
        dto.setDoLech(doLech);

        result = dto;
        dispose();
    }

    public static MajorCombinationDTO showDialog(java.awt.Component parent, MajorCombinationDTO source, boolean editMode) {
        Frame owner = JOptionPane.getFrameForComponent(parent);
        MajorCombinationEditDialog dialog = new MajorCombinationEditDialog(owner, source, editMode);
        dialog.setVisible(true);
        return dialog.result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
