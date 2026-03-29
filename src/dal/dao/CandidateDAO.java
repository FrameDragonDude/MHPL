package dal.dao;

import dal.DBConnection;
import dto.CandidateDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
		String sql = "SELECT idthisinh, cccd, sobaodanh, ho, ten, ngay_sinh, dien_thoai, "
				+ "gioi_tinh, email, noi_sinh, doi_tuong, khu_vuc "
				+ "FROM xt_thisinhxettuyen25 "
				+ "WHERE (? = '' OR cccd LIKE ?) "
				+ "AND (? = '' OR CONCAT(IFNULL(ho, ''), ' ', IFNULL(ten, '')) LIKE ?) "
				+ "ORDER BY idthisinh ASC "
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
		return candidate;
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
}
