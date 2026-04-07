package gui.dialogs;

import dto.CandidateDTO;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

public final class CandidateFormDialog {

    private CandidateFormDialog() {
    }

    public static CandidateDTO showDialog(Component parent, CandidateDTO base, boolean isEdit) {
        JTextField tfCccd = new JTextField(emptyIfNull(base.getCccd()), 14);
        JTextField tfHo = new JTextField(emptyIfNull(base.getHo()), 14);
        JTextField tfTen = new JTextField(emptyIfNull(base.getTen()), 14);
        JTextField tfNgaySinh = new JTextField(emptyIfNull(base.getNgaySinh()), 10);
        JTextField tfGioiTinh = new JTextField(emptyIfNull(base.getGioiTinh()), 8);
        JTextField tfDienThoai = new JTextField(emptyIfNull(base.getDienThoai()), 12);
        JTextField tfEmail = new JTextField(emptyIfNull(base.getEmail()), 18);
        JTextField tfNoiSinh = new JTextField(emptyIfNull(base.getNoiSinh()), 12);
        JTextField tfDoiTuong = new JTextField(emptyIfNull(base.getDoiTuong()), 8);
        JTextField tfKhuVuc = new JTextField(emptyIfNull(base.getKhuVuc()), 8);

        JTextField tfTo = new JTextField(toText(base.getDiemTo()), 6);
        JTextField tfVa = new JTextField(toText(base.getDiemVa()), 6);
        JTextField tfLi = new JTextField(toText(base.getDiemLi()), 6);
        JTextField tfHoD = new JTextField(toText(base.getDiemHo()), 6);
        JTextField tfSi = new JTextField(toText(base.getDiemSi()), 6);
        JTextField tfSu = new JTextField(toText(base.getDiemSu()), 6);
        JTextField tfDi = new JTextField(toText(base.getDiemDi()), 6);
        JTextField tfGdcd = new JTextField(toText(base.getDiemGdcd()), 6);
        JTextField tfNn = new JTextField(toText(base.getDiemNn()), 6);
        JTextField tfMaMonNn = new JTextField(emptyIfNull(base.getMaMonNn()), 8);
        JTextField tfKtpl = new JTextField(toText(base.getDiemKtpl()), 6);
        JTextField tfTi = new JTextField(toText(base.getDiemTi()), 6);
        JTextField tfCncn = new JTextField(toText(base.getDiemCncn()), 6);
        JTextField tfCnnn = new JTextField(toText(base.getDiemCnnn()), 6);
        JTextField tfChuongTrinh = new JTextField(emptyIfNull(base.getChuongTrinh()), 10);

        JTextField tfNk1 = new JTextField(toText(base.getDiemNk1()), 6);
        JTextField tfNk2 = new JTextField(toText(base.getDiemNk2()), 6);
        JTextField tfNk3 = new JTextField(toText(base.getDiemNk3()), 6);
        JTextField tfNk4 = new JTextField(toText(base.getDiemNk4()), 6);
        JTextField tfNk5 = new JTextField(toText(base.getDiemNk5()), 6);
        JTextField tfNk6 = new JTextField(toText(base.getDiemNk6()), 6);
        JTextField tfNk7 = new JTextField(toText(base.getDiemNk7()), 6);
        JTextField tfNk8 = new JTextField(toText(base.getDiemNk8()), 6);
        JTextField tfNk9 = new JTextField(toText(base.getDiemNk9()), 6);
        JTextField tfNk10 = new JTextField(toText(base.getDiemNk10()), 6);
        JTextField tfDiemXtn = new JTextField(toText(base.getDiemXetTotNghiep()), 6);
        JTextField tfDanToc = new JTextField(emptyIfNull(base.getDanToc()), 10);
        JTextField tfMaDanToc = new JTextField(emptyIfNull(base.getMaDanToc()), 6);

        JPanel form = new JPanel(new GridLayout(0, 4, 6, 6));
        form.add(new JLabel("CCCD")); form.add(tfCccd);
        form.add(new JLabel("Họ")); form.add(tfHo);
        form.add(new JLabel("Tên")); form.add(tfTen);
        form.add(new JLabel("Ngày sinh")); form.add(tfNgaySinh);
        form.add(new JLabel("Giới tính")); form.add(tfGioiTinh);
        form.add(new JLabel("Số điện thoại")); form.add(tfDienThoai);
        form.add(new JLabel("Email")); form.add(tfEmail);
        form.add(new JLabel("Nơi sinh")); form.add(tfNoiSinh);
        form.add(new JLabel("ĐTƯT")); form.add(tfDoiTuong);
        form.add(new JLabel("KVƯT")); form.add(tfKhuVuc);
        form.add(new JLabel("TO")); form.add(tfTo);
        form.add(new JLabel("VA")); form.add(tfVa);
        form.add(new JLabel("LI")); form.add(tfLi);
        form.add(new JLabel("HO")); form.add(tfHoD);
        form.add(new JLabel("SI")); form.add(tfSi);
        form.add(new JLabel("SU")); form.add(tfSu);
        form.add(new JLabel("DI")); form.add(tfDi);
        form.add(new JLabel("GDCD")); form.add(tfGdcd);
        form.add(new JLabel("NN")); form.add(tfNn);
        form.add(new JLabel("Mã môn NN")); form.add(tfMaMonNn);
        form.add(new JLabel("KTPL")); form.add(tfKtpl);
        form.add(new JLabel("TI")); form.add(tfTi);
        form.add(new JLabel("CNCN")); form.add(tfCncn);
        form.add(new JLabel("CNNN")); form.add(tfCnnn);
        form.add(new JLabel("Chương trình")); form.add(tfChuongTrinh);
        form.add(new JLabel("NK1")); form.add(tfNk1);
        form.add(new JLabel("NK2")); form.add(tfNk2);
        form.add(new JLabel("NK3")); form.add(tfNk3);
        form.add(new JLabel("NK4")); form.add(tfNk4);
        form.add(new JLabel("NK5")); form.add(tfNk5);
        form.add(new JLabel("NK6")); form.add(tfNk6);
        form.add(new JLabel("NK7")); form.add(tfNk7);
        form.add(new JLabel("NK8")); form.add(tfNk8);
        form.add(new JLabel("NK9")); form.add(tfNk9);
        form.add(new JLabel("NK10")); form.add(tfNk10);
        form.add(new JLabel("Điểm xét tốt nghiệp")); form.add(tfDiemXtn);
        form.add(new JLabel("Dân tộc")); form.add(tfDanToc);
        form.add(new JLabel("Mã dân tộc")); form.add(tfMaDanToc);

        JScrollPane sp = new JScrollPane(form);
        sp.setPreferredSize(new Dimension(760, 460));

        int result = JOptionPane.showConfirmDialog(
                parent,
                sp,
                isEdit ? "Sửa thí sinh" : "Thêm thí sinh",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String cccd = tfCccd.getText().trim();
        String ten = tfTen.getText().trim();
        if (cccd.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "CCCD và Tên không được để trống.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        CandidateDTO out = new CandidateDTO();
        out.setCccd(cccd);
        out.setHo(tfHo.getText().trim());
        out.setTen(ten);
        out.setNgaySinh(tfNgaySinh.getText().trim());
        out.setGioiTinh(tfGioiTinh.getText().trim());
        out.setDienThoai(tfDienThoai.getText().trim());
        out.setEmail(tfEmail.getText().trim());
        out.setNoiSinh(tfNoiSinh.getText().trim());
        out.setDoiTuong(tfDoiTuong.getText().trim());
        out.setKhuVuc(tfKhuVuc.getText().trim());

        out.setDiemTo(parseDoubleOrNull(tfTo.getText()));
        out.setDiemVa(parseDoubleOrNull(tfVa.getText()));
        out.setDiemLi(parseDoubleOrNull(tfLi.getText()));
        out.setDiemHo(parseDoubleOrNull(tfHoD.getText()));
        out.setDiemSi(parseDoubleOrNull(tfSi.getText()));
        out.setDiemSu(parseDoubleOrNull(tfSu.getText()));
        out.setDiemDi(parseDoubleOrNull(tfDi.getText()));
        out.setDiemGdcd(parseDoubleOrNull(tfGdcd.getText()));
        out.setDiemNn(parseDoubleOrNull(tfNn.getText()));
        out.setMaMonNn(tfMaMonNn.getText().trim());
        out.setDiemKtpl(parseDoubleOrNull(tfKtpl.getText()));
        out.setDiemTi(parseDoubleOrNull(tfTi.getText()));
        out.setDiemCncn(parseDoubleOrNull(tfCncn.getText()));
        out.setDiemCnnn(parseDoubleOrNull(tfCnnn.getText()));
        out.setChuongTrinh(tfChuongTrinh.getText().trim());

        out.setDiemNk1(parseDoubleOrNull(tfNk1.getText()));
        out.setDiemNk2(parseDoubleOrNull(tfNk2.getText()));
        out.setDiemNk3(parseDoubleOrNull(tfNk3.getText()));
        out.setDiemNk4(parseDoubleOrNull(tfNk4.getText()));
        out.setDiemNk5(parseDoubleOrNull(tfNk5.getText()));
        out.setDiemNk6(parseDoubleOrNull(tfNk6.getText()));
        out.setDiemNk7(parseDoubleOrNull(tfNk7.getText()));
        out.setDiemNk8(parseDoubleOrNull(tfNk8.getText()));
        out.setDiemNk9(parseDoubleOrNull(tfNk9.getText()));
        out.setDiemNk10(parseDoubleOrNull(tfNk10.getText()));
        out.setDiemXetTotNghiep(parseDoubleOrNull(tfDiemXtn.getText()));
        out.setDanToc(tfDanToc.getText().trim());
        out.setMaDanToc(tfMaDanToc.getText().trim());

        return out;
    }

    private static Double parseDoubleOrNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        if (t.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String toText(Double value) {
        return value == null ? "" : String.format("%.2f", value);
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }
}
