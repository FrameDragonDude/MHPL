package dal.dao;

import dto.EnglishConversionDTO;
import dto.PriorityAdmissionDTO;
import org.hibernate.Session;
import org.hibernate.Transaction;
import dal.hibernate.HibernateUtil;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class AdmissionDAO {
    public void saveEnglishConversionData(List<EnglishConversionDTO> dtoList) throws SQLException {
        if (dtoList == null || dtoList.isEmpty()) return;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.doWork(conn -> {
                    String sql = "INSERT INTO xt_quydoi_tienganh (cccd, chung_chi, diem_goc, diem_quydoi, diem_cong) "
                               + "VALUES (?, ?, ?, ?, ?) "
                               + "ON DUPLICATE KEY UPDATE chung_chi=VALUES(chung_chi), diem_goc=VALUES(diem_goc), "
                               + "diem_quydoi=VALUES(diem_quydoi), diem_cong=VALUES(diem_cong)";

                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        for (EnglishConversionDTO dto : dtoList) {
                            ps.setString(1, dto.getCccd());
                            ps.setString(2, dto.getChungChi());
                            ps.setString(3, dto.getDiemGoc());
                            ps.setBigDecimal(4, dto.getDiemQuydoi());
                            ps.setBigDecimal(5, dto.getDiemCong());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                });
                tx.commit();
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) tx.rollback();
                throw new SQLException("Lỗi tầng DAO khi import Tiếng Anh: " + ex.getMessage(), ex);
            }
        }
    }

    public void savePriorityAdmissionData(List<PriorityAdmissionDTO> dtoList) throws SQLException {
        if (dtoList == null || dtoList.isEmpty()) return;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.doWork(conn -> {
                    // Xóa sạch dữ liệu cũ để nạp mới hoàn toàn
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("TRUNCATE TABLE xt_uutien_xettuyen");
                    }

                    String sql = "INSERT INTO xt_uutien_xettuyen (cccd, cap, doi_tuong, ma_mon, loai_giai, "
                               + "diem_cong_trung_mon, diem_cong_khac_mon) VALUES (?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        for (PriorityAdmissionDTO dto : dtoList) {
                            ps.setString(1, dto.getCccd());
                            ps.setString(2, dto.getCap());
                            ps.setString(3, dto.getDoiTuong());
                            ps.setString(4, dto.getMaMon());
                            ps.setString(5, dto.getLoaiGiai());
                            ps.setBigDecimal(6, dto.getDiemCongTrungMon());
                            ps.setBigDecimal(7, dto.getDiemCongKhacMon());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                });
                tx.commit();
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) tx.rollback();
                throw new SQLException("Lỗi tầng DAO khi import Ưu tiên xét tuyển: " + ex.getMessage(), ex);
            }
        }
    }

    public EnglishConversionDTO getEnglishConversionByCccd(String cccd) throws SQLException {
        final EnglishConversionDTO[] wrapper = new EnglishConversionDTO[1];
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(new org.hibernate.jdbc.Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    String sql = "SELECT cccd, chung_chi, diem_goc, diem_quydoi, diem_cong FROM xt_quydoi_tienganh WHERE cccd = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, cccd);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                EnglishConversionDTO dto = new EnglishConversionDTO();
                                dto.setCccd(rs.getString("cccd"));
                                dto.setChungChi(rs.getString("chung_chi"));
                                dto.setDiemGoc(rs.getString("diem_goc"));
                                dto.setDiemQuydoi(rs.getBigDecimal("diem_quydoi"));
                                dto.setDiemCong(rs.getBigDecimal("diem_cong"));
                                wrapper[0] = dto;
                            }
                        }
                    }
                }
            });
        } catch (Exception ex) {
            throw new SQLException("Lỗi SQL lấy thông tin quy đổi tiếng Anh: " + ex.getMessage(), ex);
        }
        return wrapper[0];
    }

    public BigDecimal[] getPriorityScores(String cccd, List<String> comboSubjects) throws SQLException {
        final BigDecimal[][] wrapper = new BigDecimal[1][];
        wrapper[0] = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(new org.hibernate.jdbc.Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    String sql = "SELECT ma_mon, diem_cong_trung_mon, diem_cong_khac_mon FROM xt_uutien_xettuyen WHERE cccd = ? LIMIT 1";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, cccd);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                String maMonDatGiai = rs.getString("ma_mon");
                                BigDecimal trungMon = rs.getBigDecimal("diem_cong_trung_mon");
                                BigDecimal khacMon = rs.getBigDecimal("diem_cong_khac_mon");

                                if (comboSubjects != null && comboSubjects.contains(maMonDatGiai)) {
                                    wrapper[0] = new BigDecimal[]{trungMon, BigDecimal.ZERO};
                                } else {
                                    wrapper[0] = new BigDecimal[]{BigDecimal.ZERO, khacMon};
                                }
                            }
                        }
                    }
                }
            });
        } catch (Exception ex) {
            throw new SQLException("Lỗi SQL tính điểm ưu tiên xét tuyển: " + ex.getMessage(), ex);
        }
        return wrapper[0];
    }

    public Map<String, Object> getMajorCombinationConfig(String maNganh, String maToHop) throws SQLException {
        final Map<String, Object>[] wrapper = new Map[1];

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(new org.hibernate.jdbc.Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    String sql = "SELECT nt.do_lech, th.mon1, th.mon2, th.mon3, " +
                                 "nt.hs_mon1, nt.hs_mon2, nt.hs_mon3 " +
                                 "FROM xt_nganh_tohop nt " +
                                 "JOIN xt_tohop_monthi th ON nt.matohop = th.matohop " +
                                 "WHERE nt.manganh = ? AND nt.matohop = ?";
                    
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, maNganh);
                        ps.setString(2, maToHop);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                Map<String, Object> config = new HashMap<>();
                                config.put("doLech", rs.getBigDecimal("do_lech"));
                                config.put("mon1", rs.getString("mon1"));
                                config.put("mon2", rs.getString("mon2"));
                                config.put("mon3", rs.getString("mon3"));
                                
                                int hs1 = rs.getInt("hs_mon1");
                                int hs2 = rs.getInt("hs_mon2");
                                int hs3 = rs.getInt("hs_mon3");
                                
                                config.put("hs_mon1", hs1 == 0 ? 1 : hs1);
                                config.put("hs_mon2", hs2 == 0 ? 1 : hs2);
                                config.put("hs_mon3", hs3 == 0 ? 1 : hs3);
                                wrapper[0] = config;
                            }
                        }
                    }
                }
            });
        } catch (Exception ex) {
            throw new SQLException("Lỗi SQL lấy cấu hình ngành và tổ hợp: " + ex.getMessage(), ex);
        }
        return wrapper[0];
    }

    public Map<String, BigDecimal> findConversionRange(String phuongThuc, String mon, BigDecimal diemGoc) throws SQLException {
        final Map<String, BigDecimal> range = new HashMap<>();
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(new org.hibernate.jdbc.Work() {
                @Override
                public void execute(Connection conn) throws SQLException {
                    
                    String sql;
                    if (mon == null) {
                        sql = "SELECT d_diema, d_diemb, d_diemc, d_diemd FROM xt_bangquydoi " +
                              "WHERE d_phuongthuc = ? AND d_mon IS NULL AND ? >= d_diema AND ? <= d_diemb LIMIT 1";
                    } else {
                        sql = "SELECT d_diema, d_diemb, d_diemc, d_diemd FROM xt_bangquydoi " +
                              "WHERE d_phuongthuc = ? AND d_mon = ? AND ? >= d_diema AND ? <= d_diemb LIMIT 1";
                    }
                    
                    try (PreparedStatement ops = conn.prepareStatement(sql)) {
                        if (mon == null) {
                            ops.setString(1, phuongThuc);
                            ops.setBigDecimal(2, diemGoc);
                            ops.setBigDecimal(3, diemGoc);
                        } else {
                            ops.setString(1, phuongThuc);
                            ops.setString(2, mon);
                            ops.setBigDecimal(3, diemGoc);
                            ops.setBigDecimal(4, diemGoc);
                        }
                        
                        try (ResultSet rs = ops.executeQuery()) {
                            if (rs.next()) {
                                range.put("a", rs.getBigDecimal("d_diema"));
                                range.put("b", rs.getBigDecimal("d_diemb"));
                                range.put("c", rs.getBigDecimal("d_diemc"));
                                range.put("d", rs.getBigDecimal("d_diemd"));
                            }
                        }
                    }
                }
            });
        } catch (Exception ex) {
            throw new SQLException("Lỗi SQL khi tìm khoảng quy đổi bách phân vị: " + ex.getMessage(), ex);
        }
        return range.isEmpty() ? null : range;
    }

}