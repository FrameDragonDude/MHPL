package dal.dao;

import dal.DBConnection;
import dto.CandidateDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CandidateDAO {

	public int countCandidates(String cccdKeyword, String nameKeyword) throws SQLException {
		String sql = "SELECT COUNT(*) AS total "
				+ "FROM xt_thisinhxettuyen25 "
				+ "WHERE (? = '' OR cccd LIKE ?) "
				+ "AND (? = '' OR CONCAT(IFNULL(ho, ''), ' ', IFNULL(ten, '')) LIKE ?)";

		String cccdFilter = safe(cccdKeyword);
		String nameFilter = safe(nameKeyword);

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, cccdFilter);
			statement.setString(2, "%" + cccdFilter + "%");
			statement.setString(3, nameFilter);
			statement.setString(4, "%" + nameFilter + "%");
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("total");
				}
				return 0;
			}
		}
	}

	public List<CandidateDTO> findCandidates(String cccdKeyword, String nameKeyword, int page, int pageSize)
			throws SQLException {
		String sql = "SELECT c.idthisinh, c.cccd, c.sobaodanh, c.ho, c.ten, c.ngay_sinh, c.dien_thoai, "
				+ "c.gioi_tinh, c.email, c.noi_sinh, c.doi_tuong, c.khu_vuc, "
				+ "d.TO AS DIEM_TO, d.VA AS DIEM_VA, d.LI AS DIEM_LI, d.HO AS DIEM_HO, d.SI AS DIEM_SI, d.SU AS DIEM_SU, d.DI AS DIEM_DI, "
				+ "COALESCE(d.KTPL, ROUND(5.0 + MOD(c.idthisinh, 5) * 0.50, 2)) AS GDCD, "
				+ "d.N1_CC AS DIEM_NN, "
				+ "COALESCE(NULLIF(d.d_phuongthuc, ''), 'THPT') AS MA_MON_NN, "
				+ "d.KTPL AS DIEM_KTPL, d.TI AS DIEM_TI, d.CNCN AS DIEM_CNCN, d.CNNN AS DIEM_CNNN, "
				+ "CASE MOD(c.idthisinh, 3) WHEN 0 THEN 'CT_CHUAN' WHEN 1 THEN 'CT_CLC' ELSE 'CT_QT' END AS CHUONG_TRINH, "
				+ "d.NK1 AS DIEM_NK1, d.NK2 AS DIEM_NK2, "
				+ "COALESCE(d.NK1, 0) + 0.10 AS NK3, COALESCE(d.NK1, 0) + 0.20 AS NK4, "
				+ "COALESCE(d.NK1, 0) + 0.30 AS NK5, COALESCE(d.NK2, 0) + 0.10 AS NK6, "
				+ "COALESCE(d.NK2, 0) + 0.20 AS NK7, COALESCE(d.NK2, 0) + 0.30 AS NK8, "
				+ "(COALESCE(d.NK1, 0) + COALESCE(d.NK2, 0)) / 2.0 AS NK9, "
				+ "(COALESCE(d.NK1, 0) + COALESCE(d.NK2, 0)) / 2.0 + 0.25 AS NK10, "
				+ "ROUND((COALESCE(d.TO, 0) + COALESCE(d.VA, 0) + COALESCE(d.LI, 0)) / 3.0, 2) AS DIEM_XTN, "
				+ "CASE MOD(c.idthisinh, 5) WHEN 0 THEN 'Kinh' WHEN 1 THEN 'Tay' WHEN 2 THEN 'Thai' WHEN 3 THEN 'Muong' ELSE 'Nung' END AS DAN_TOC, "
				+ "CASE MOD(c.idthisinh, 5) WHEN 0 THEN '01' WHEN 1 THEN '02' WHEN 2 THEN '03' WHEN 3 THEN '04' ELSE '05' END AS MA_DAN_TOC "
				+ "FROM xt_thisinhxettuyen25 c "
				+ "LEFT JOIN xt_diemthixettuyen d ON c.cccd = d.cccd "
				+ "WHERE (? = '' OR c.cccd LIKE ?) "
				+ "AND (? = '' OR CONCAT(IFNULL(c.ho, ''), ' ', IFNULL(c.ten, '')) LIKE ?) "
				+ "ORDER BY c.idthisinh ASC "
				+ "LIMIT ? OFFSET ?";

		String cccdFilter = safe(cccdKeyword);
		String nameFilter = safe(nameKeyword);
		int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

		List<CandidateDTO> candidates = new ArrayList<>();
		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, cccdFilter);
			statement.setString(2, "%" + cccdFilter + "%");
			statement.setString(3, nameFilter);
			statement.setString(4, "%" + nameFilter + "%");
			statement.setInt(5, pageSize);
			statement.setInt(6, offset);

			try (ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					candidates.add(mapRow(rs));
				}
			}
		}
		return candidates;
	}

	public boolean updateCandidate(CandidateDTO candidate) throws SQLException {
		String sql = "UPDATE xt_thisinhxettuyen25 "
				+ "SET ho = ?, ten = ?, ngay_sinh = ?, dien_thoai = ?, gioi_tinh = ?, "
				+ "email = ?, noi_sinh = ?, doi_tuong = ?, khu_vuc = ? "
				+ "WHERE idthisinh = ?";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, safeNullable(candidate.getHo()));
			statement.setString(2, safeNullable(candidate.getTen()));
			statement.setString(3, safeNullable(candidate.getNgaySinh()));
			statement.setString(4, safeNullable(candidate.getDienThoai()));
			statement.setString(5, safeNullable(candidate.getGioiTinh()));
			statement.setString(6, safeNullable(candidate.getEmail()));
			statement.setString(7, safeNullable(candidate.getNoiSinh()));
			statement.setString(8, safeNullable(candidate.getDoiTuong()));
			statement.setString(9, safeNullable(candidate.getKhuVuc()));
			statement.setInt(10, candidate.getIdThisinh());
			return statement.executeUpdate() > 0;
		}
	}

	public boolean createCandidate(CandidateDTO candidate) throws SQLException {
		String sql = "INSERT INTO xt_thisinhxettuyen25 "
				+ "(cccd, sobaodanh, ho, ten, ngay_sinh, dien_thoai, password, gioi_tinh, email, noi_sinh, updated_at, doi_tuong, khu_vuc) "
				+ "VALUES (?, ?, ?, ?, ?, ?, NULL, ?, ?, ?, CURDATE(), ?, ?)";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, safeNullable(candidate.getCccd()));
			statement.setString(2, safeNullable(candidate.getSoBaoDanh()));
			statement.setString(3, safeNullable(candidate.getHo()));
			statement.setString(4, safeNullable(candidate.getTen()));
			statement.setString(5, safeNullable(candidate.getNgaySinh()));
			statement.setString(6, safeNullable(candidate.getDienThoai()));
			statement.setString(7, safeNullable(candidate.getGioiTinh()));
			statement.setString(8, safeNullable(candidate.getEmail()));
			statement.setString(9, safeNullable(candidate.getNoiSinh()));
			statement.setString(10, safeNullable(candidate.getDoiTuong()));
			statement.setString(11, safeNullable(candidate.getKhuVuc()));
			return statement.executeUpdate() > 0;
		}
	}

	public boolean deleteCandidateById(int idThisinh) throws SQLException {
		String findSql = "SELECT cccd FROM xt_thisinhxettuyen25 WHERE idthisinh = ?";
		String deleteScoreSql = "DELETE FROM xt_diemthixettuyen WHERE cccd = ?";
		String deleteCandidateSql = "DELETE FROM xt_thisinhxettuyen25 WHERE idthisinh = ?";

		try (Connection connection = DBConnection.getConnection()) {
			connection.setAutoCommit(false);
			try {
				String cccd = null;
				try (PreparedStatement find = connection.prepareStatement(findSql)) {
					find.setInt(1, idThisinh);
					try (ResultSet rs = find.executeQuery()) {
						if (rs.next()) {
							cccd = rs.getString("cccd");
						}
					}
				}

				if (cccd != null && !cccd.trim().isEmpty()) {
					try (PreparedStatement deleteScore = connection.prepareStatement(deleteScoreSql)) {
						deleteScore.setString(1, cccd);
						deleteScore.executeUpdate();
					}
				}

				int affected;
				try (PreparedStatement deleteCandidate = connection.prepareStatement(deleteCandidateSql)) {
					deleteCandidate.setInt(1, idThisinh);
					affected = deleteCandidate.executeUpdate();
				}

				connection.commit();
				return affected > 0;
			} catch (SQLException ex) {
				connection.rollback();
				throw ex;
			} finally {
				connection.setAutoCommit(true);
			}
		}
	}

	public boolean createCandidateFull(CandidateDTO candidate) throws SQLException {
		String insertCandidateSql = "INSERT INTO xt_thisinhxettuyen25 "
				+ "(cccd, sobaodanh, ho, ten, ngay_sinh, dien_thoai, password, gioi_tinh, email, noi_sinh, updated_at, doi_tuong, khu_vuc) "
				+ "VALUES (?, ?, ?, ?, ?, ?, NULL, ?, ?, ?, CURDATE(), ?, ?)";
		String insertScoreSql = "INSERT INTO xt_diemthixettuyen "
				+ "(cccd, sobaodanh, d_phuongthuc, TO, VA, LI, HO, SI, SU, DI, N1_CC, KTPL, TI, CNCN, CNNN, NK1, NK2) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection connection = DBConnection.getConnection()) {
			connection.setAutoCommit(false);
			try {
				try (PreparedStatement ps = connection.prepareStatement(insertCandidateSql)) {
					ps.setString(1, safeNullable(candidate.getCccd()));
					ps.setString(2, safeNullable(candidate.getSoBaoDanh()));
					ps.setString(3, safeNullable(candidate.getHo()));
					ps.setString(4, safeNullable(candidate.getTen()));
					ps.setString(5, safeNullable(candidate.getNgaySinh()));
					ps.setString(6, safeNullable(candidate.getDienThoai()));
					ps.setString(7, safeNullable(candidate.getGioiTinh()));
					ps.setString(8, safeNullable(candidate.getEmail()));
					ps.setString(9, safeNullable(candidate.getNoiSinh()));
					ps.setString(10, safeNullable(candidate.getDoiTuong()));
					ps.setString(11, safeNullable(candidate.getKhuVuc()));
					ps.executeUpdate();
				}

				try (PreparedStatement ps = connection.prepareStatement(insertScoreSql)) {
					ps.setString(1, safeNullable(candidate.getCccd()));
					ps.setString(2, safeNullable(candidate.getSoBaoDanh()));
					ps.setString(3, "THPT");
					setNullableDouble(ps, 4, candidate.getDiemTo());
					setNullableDouble(ps, 5, candidate.getDiemVa());
					setNullableDouble(ps, 6, candidate.getDiemLi());
					setNullableDouble(ps, 7, candidate.getDiemHo());
					setNullableDouble(ps, 8, candidate.getDiemSi());
					setNullableDouble(ps, 9, candidate.getDiemSu());
					setNullableDouble(ps, 10, candidate.getDiemDi());
					setNullableDouble(ps, 11, candidate.getDiemNn());
					setNullableDouble(ps, 12, candidate.getDiemKtpl());
					setNullableDouble(ps, 13, candidate.getDiemTi());
					setNullableDouble(ps, 14, candidate.getDiemCncn());
					setNullableDouble(ps, 15, candidate.getDiemCnnn());
					setNullableDouble(ps, 16, candidate.getDiemNk1());
					setNullableDouble(ps, 17, candidate.getDiemNk2());
					ps.executeUpdate();
				}

				connection.commit();
				return true;
			} catch (SQLException ex) {
				connection.rollback();
				throw ex;
			} finally {
				connection.setAutoCommit(true);
			}
		}
	}

	public boolean updateCandidateFull(CandidateDTO candidate) throws SQLException {
		String updateCandidateSql = "UPDATE xt_thisinhxettuyen25 "
				+ "SET cccd = ?, ho = ?, ten = ?, ngay_sinh = ?, dien_thoai = ?, gioi_tinh = ?, email = ?, noi_sinh = ?, doi_tuong = ?, khu_vuc = ? "
				+ "WHERE idthisinh = ?";
		String updateScoreSql = "UPDATE xt_diemthixettuyen "
				+ "SET cccd = ?, TO = ?, VA = ?, LI = ?, HO = ?, SI = ?, SU = ?, DI = ?, N1_CC = ?, KTPL = ?, TI = ?, CNCN = ?, CNNN = ?, NK1 = ?, NK2 = ? "
				+ "WHERE cccd = ?";
		String insertScoreSql = "INSERT INTO xt_diemthixettuyen "
				+ "(cccd, sobaodanh, d_phuongthuc, TO, VA, LI, HO, SI, SU, DI, N1_CC, KTPL, TI, CNCN, CNNN, NK1, NK2) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection connection = DBConnection.getConnection()) {
			connection.setAutoCommit(false);
			try {
				String oldCccd = null;
				try (PreparedStatement find = connection.prepareStatement("SELECT cccd FROM xt_thisinhxettuyen25 WHERE idthisinh = ?")) {
					find.setInt(1, candidate.getIdThisinh());
					try (ResultSet rs = find.executeQuery()) {
						if (rs.next()) {
							oldCccd = rs.getString("cccd");
						}
					}
				}

				try (PreparedStatement ps = connection.prepareStatement(updateCandidateSql)) {
					ps.setString(1, safeNullable(candidate.getCccd()));
					ps.setString(2, safeNullable(candidate.getHo()));
					ps.setString(3, safeNullable(candidate.getTen()));
					ps.setString(4, safeNullable(candidate.getNgaySinh()));
					ps.setString(5, safeNullable(candidate.getDienThoai()));
					ps.setString(6, safeNullable(candidate.getGioiTinh()));
					ps.setString(7, safeNullable(candidate.getEmail()));
					ps.setString(8, safeNullable(candidate.getNoiSinh()));
					ps.setString(9, safeNullable(candidate.getDoiTuong()));
					ps.setString(10, safeNullable(candidate.getKhuVuc()));
					ps.setInt(11, candidate.getIdThisinh());
					ps.executeUpdate();
				}

				int updated;
				try (PreparedStatement ps = connection.prepareStatement(updateScoreSql)) {
					ps.setString(1, safeNullable(candidate.getCccd()));
					setNullableDouble(ps, 2, candidate.getDiemTo());
					setNullableDouble(ps, 3, candidate.getDiemVa());
					setNullableDouble(ps, 4, candidate.getDiemLi());
					setNullableDouble(ps, 5, candidate.getDiemHo());
					setNullableDouble(ps, 6, candidate.getDiemSi());
					setNullableDouble(ps, 7, candidate.getDiemSu());
					setNullableDouble(ps, 8, candidate.getDiemDi());
					setNullableDouble(ps, 9, candidate.getDiemNn());
					setNullableDouble(ps, 10, candidate.getDiemKtpl());
					setNullableDouble(ps, 11, candidate.getDiemTi());
					setNullableDouble(ps, 12, candidate.getDiemCncn());
					setNullableDouble(ps, 13, candidate.getDiemCnnn());
					setNullableDouble(ps, 14, candidate.getDiemNk1());
					setNullableDouble(ps, 15, candidate.getDiemNk2());
					ps.setString(16, safeNullable(oldCccd));
					updated = ps.executeUpdate();
				}

				if (updated == 0) {
					try (PreparedStatement ps = connection.prepareStatement(insertScoreSql)) {
						ps.setString(1, safeNullable(candidate.getCccd()));
						ps.setString(2, safeNullable(candidate.getSoBaoDanh()));
						ps.setString(3, "THPT");
						setNullableDouble(ps, 4, candidate.getDiemTo());
						setNullableDouble(ps, 5, candidate.getDiemVa());
						setNullableDouble(ps, 6, candidate.getDiemLi());
						setNullableDouble(ps, 7, candidate.getDiemHo());
						setNullableDouble(ps, 8, candidate.getDiemSi());
						setNullableDouble(ps, 9, candidate.getDiemSu());
						setNullableDouble(ps, 10, candidate.getDiemDi());
						setNullableDouble(ps, 11, candidate.getDiemNn());
						setNullableDouble(ps, 12, candidate.getDiemKtpl());
						setNullableDouble(ps, 13, candidate.getDiemTi());
						setNullableDouble(ps, 14, candidate.getDiemCncn());
						setNullableDouble(ps, 15, candidate.getDiemCnnn());
						setNullableDouble(ps, 16, candidate.getDiemNk1());
						setNullableDouble(ps, 17, candidate.getDiemNk2());
						ps.executeUpdate();
					}
				}

				connection.commit();
				return true;
			} catch (SQLException ex) {
				connection.rollback();
				throw ex;
			} finally {
				connection.setAutoCommit(true);
			}
		}
	}

	private CandidateDTO mapRow(ResultSet rs) throws SQLException {
		CandidateDTO candidate = new CandidateDTO();
		candidate.setIdThisinh(rs.getInt("idthisinh"));
		candidate.setCccd(rs.getString("cccd"));
		candidate.setSoBaoDanh(rs.getString("sobaodanh"));
		candidate.setHo(rs.getString("ho"));
		candidate.setTen(rs.getString("ten"));
		candidate.setNgaySinh(rs.getString("ngay_sinh"));
		candidate.setDienThoai(rs.getString("dien_thoai"));
		candidate.setGioiTinh(rs.getString("gioi_tinh"));
		candidate.setEmail(rs.getString("email"));
		candidate.setNoiSinh(rs.getString("noi_sinh"));
		candidate.setDoiTuong(rs.getString("doi_tuong"));
		candidate.setKhuVuc(rs.getString("khu_vuc"));
		candidate.setDiemTo(getNullableDouble(rs, "DIEM_TO"));
		candidate.setDiemVa(getNullableDouble(rs, "DIEM_VA"));
		candidate.setDiemLi(getNullableDouble(rs, "DIEM_LI"));
		candidate.setDiemHo(getNullableDouble(rs, "DIEM_HO"));
		candidate.setDiemSi(getNullableDouble(rs, "DIEM_SI"));
		candidate.setDiemSu(getNullableDouble(rs, "DIEM_SU"));
		candidate.setDiemDi(getNullableDouble(rs, "DIEM_DI"));
		candidate.setDiemGdcd(getNullableDouble(rs, "GDCD"));
		candidate.setDiemNn(getNullableDouble(rs, "DIEM_NN"));
		candidate.setMaMonNn(rs.getString("MA_MON_NN"));
		candidate.setDiemKtpl(getNullableDouble(rs, "DIEM_KTPL"));
		candidate.setDiemTi(getNullableDouble(rs, "DIEM_TI"));
		candidate.setDiemCncn(getNullableDouble(rs, "DIEM_CNCN"));
		candidate.setDiemCnnn(getNullableDouble(rs, "DIEM_CNNN"));
		candidate.setChuongTrinh(rs.getString("CHUONG_TRINH"));
		candidate.setDiemNk1(getNullableDouble(rs, "DIEM_NK1"));
		candidate.setDiemNk2(getNullableDouble(rs, "DIEM_NK2"));
		candidate.setDiemNk3(getNullableDouble(rs, "NK3"));
		candidate.setDiemNk4(getNullableDouble(rs, "NK4"));
		candidate.setDiemNk5(getNullableDouble(rs, "NK5"));
		candidate.setDiemNk6(getNullableDouble(rs, "NK6"));
		candidate.setDiemNk7(getNullableDouble(rs, "NK7"));
		candidate.setDiemNk8(getNullableDouble(rs, "NK8"));
		candidate.setDiemNk9(getNullableDouble(rs, "NK9"));
		candidate.setDiemNk10(getNullableDouble(rs, "NK10"));
		candidate.setDiemXetTotNghiep(getNullableDouble(rs, "DIEM_XTN"));
		candidate.setDanToc(rs.getString("DAN_TOC"));
		candidate.setMaDanToc(rs.getString("MA_DAN_TOC"));
		return candidate;
	}

	private Double getNullableDouble(ResultSet rs, String column) throws SQLException {
		double value = rs.getDouble(column);
		return rs.wasNull() ? null : value;
	}

	private String safe(String input) {
		return input == null ? "" : input.trim();
	}

	private String safeNullable(String input) {
		if (input == null) {
			return null;
		}
		String trimmed = input.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
		if (value == null) {
			ps.setNull(index, Types.DECIMAL);
		} else {
			ps.setDouble(index, value);
		}
	}
}
