package gui.dialogs;

import dto.ExamScoreDTO;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ExamScoreFormDialog extends JDialog {
    private final ExamScoreDTO examScore;
    private final Map<String, JTextField> fieldMap = new HashMap<>();
    private final JComboBox<String> cbPhuongThuc;
    private boolean confirmed = false;
    private boolean isEditMode = false;

    public ExamScoreFormDialog(Frame parent, ExamScoreDTO data) {
        super(parent, data == null ? "Nhập điểm thi mới" : "Chỉnh sửa điểm thi", true);
        this.examScore = data != null ? data : new ExamScoreDTO();
        this.isEditMode = data != null;
        setLayout(new BorderLayout());
        setSize(850, 600);
        setLocationRelativeTo(parent);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Nhóm thông tin định danh
        JPanel infoPanel = createSection("Thông tin định danh", 1, 6);
        cbPhuongThuc = new JComboBox<>(new String[]{"THPT", "VSAT", "ĐGNL"});
        
        addLabelField(infoPanel, "CCCD:", createTextField("cccd", examScore.getCccd()));
        addLabelField(infoPanel, "Số báo danh:", createTextField("sbd", examScore.getSoBaoDanh()));
        infoPanel.add(new JLabel("Phương thức:"));
        infoPanel.add(cbPhuongThuc);
        if(data != null) cbPhuongThuc.setSelectedItem(examScore.getPhuongThuc());

        // Nhóm điểm môn văn hóa chính (Toán, Văn, Anh)
        JPanel corePanel = createSection("Môn cơ bản & Ngoại ngữ", 2, 4);
        addLabelField(corePanel, "Toán:", createScoreField("to", examScore.getDiemTo()));
        addLabelField(corePanel, "Văn:", createScoreField("va", examScore.getDiemVa()));
        addLabelField(corePanel, "N1 Thi (Anh):", createScoreField("n1Thi", examScore.getDiemN1Thi()));
        addLabelField(corePanel, "N1 CC (IELTS...):", createScoreField("n1Cc", examScore.getDiemN1Cc()));

        // Nhóm môn tự nhiên & xã hội
        JPanel electivePanel = createSection("Môn tự chọn", 3, 6);
        addLabelField(electivePanel, "Lý:", createScoreField("li", examScore.getDiemLi()));
        addLabelField(electivePanel, "Hóa:", createScoreField("ho", examScore.getDiemHo()));
        addLabelField(electivePanel, "Sinh:", createScoreField("si", examScore.getDiemSi()));
        addLabelField(electivePanel, "Sử:", createScoreField("su", examScore.getDiemSu()));
        addLabelField(electivePanel, "Địa:", createScoreField("di", examScore.getDiemDi()));
        addLabelField(electivePanel, "KTPL:", createScoreField("ktpl", examScore.getDiemKtpl()));

        // Nhóm môn năng khiếu & Công nghệ
        JPanel techPanel = createSection("Năng khiếu & Công nghệ", 2, 6);
        addLabelField(techPanel, "Tin học:", createScoreField("ti", examScore.getDiemTi()));
        addLabelField(techPanel, "CN Công nghiệp:", createScoreField("cncn", examScore.getDiemCncn()));
        addLabelField(techPanel, "CN Nông nghiệp:", createScoreField("cnnn", examScore.getDiemCnnn()));
        addLabelField(techPanel, "NL1:", createScoreField("nl1", examScore.getDiemNl1()));
        addLabelField(techPanel, "NK1:", createScoreField("nk1", examScore.getDiemNk1()));
        addLabelField(techPanel, "NK2:", createScoreField("nk2", examScore.getDiemNk2()));

        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(corePanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(electivePanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(techPanel);

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        add(buildActionButtons(), BorderLayout.SOUTH);
    }

    private JPanel createSection(String title, int rows, int cols) {

        JPanel p = new JPanel(new GridLayout(rows, cols, 10, 10));

        javax.swing.border.TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                title
        );

        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 15));
        border.setTitleColor(new Color(33, 150, 243));

        p.setBorder(border);
        p.setBackground(Color.WHITE);

        return p;
    }

    private void addLabelField(JPanel panel, String label, JTextField field) {
        JLabel lb = new JLabel(label);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lb);
        panel.add(field);
    }

    private JTextField createTextField(String key, String value) {
        JTextField tf = new JTextField(value);
        fieldMap.put(key, tf);
        return tf;
    }

    private JTextField createScoreField(String key, Double value) {
        JTextField tf = new JTextField(value == null ? "" : value.toString());
        fieldMap.put(key, tf);
        return tf;
    }

    private JPanel buildActionButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton(isEditMode ? "Lưu dữ liệu" : "Thêm mới");
        JButton btnCancel = new JButton("Hủy");

        styleButton(btnSave, new Color(33, 150, 243));
        styleButton(btnCancel, new Color(225, 0, 0));

        btnSave.addActionListener(e -> {
            if (validateForm()) {
                saveData();
                confirmed = true;
                dispose();
            }
        });
        btnCancel.addActionListener(e -> dispose());

        p.add(btnSave);
        p.add(btnCancel);
        return p;
    }

    private boolean validateForm() {
        if (fieldMap.get("cccd").getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "CCCD không được để trống!");
            return false;
        }
        for (Map.Entry<String, JTextField> entry : fieldMap.entrySet()) {

            String key = entry.getKey();
            JTextField tf = entry.getValue();
            String val = tf.getText().trim();

            if (key.equals("cccd") || key.equals("sbd")) continue;

            if (!val.isEmpty()) {
                try {
                    double score = Double.parseDouble(val);

                    if (score < 0 || score > 10) {
                        JOptionPane.showMessageDialog(this,
                                "Điểm phải nằm trong khoảng 0 - 10 (" + key + ")");
                        tf.requestFocus();
                        return false;
                    }

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Giá trị không hợp lệ (" + key + ")");
                    tf.requestFocus();
                    return false;
                }
            }
        }
        return true;
    }

    private void saveData() {
        examScore.setCccd(fieldMap.get("cccd").getText().trim());
        examScore.setSoBaoDanh(fieldMap.get("sbd").getText().trim());
        examScore.setPhuongThuc(cbPhuongThuc.getSelectedItem().toString());
        
        examScore.setDiemTo(parseScore("to"));
        examScore.setDiemVa(parseScore("va"));
        examScore.setDiemLi(parseScore("li"));
        examScore.setDiemHo(parseScore("ho"));
        examScore.setDiemSi(parseScore("si"));
        examScore.setDiemSu(parseScore("su"));
        examScore.setDiemDi(parseScore("di"));
        examScore.setDiemN1Thi(parseScore("n1Thi"));
        examScore.setDiemN1Cc(parseScore("n1Cc"));
        examScore.setDiemCncn(parseScore("cncn"));
        examScore.setDiemCnnn(parseScore("cnnn"));
        examScore.setDiemTi(parseScore("ti"));
        examScore.setDiemKtpl(parseScore("ktpl"));
        examScore.setDiemNl1(parseScore("nl1"));
        examScore.setDiemNk1(parseScore("nk1"));
        examScore.setDiemNk2(parseScore("nk2"));
    }

    private Double parseScore(String key) {
        String val = fieldMap.get(key).getText().trim();
        try {
            return val.isEmpty() ? null : Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
    }

    public boolean isConfirmed() { return confirmed; }
    public ExamScoreDTO getResult() { return examScore; }
}