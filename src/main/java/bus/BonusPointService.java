package bus;

import dal.dao.BonusPointDAO;
import dal.dao.MajorCombinationDAO;
import dal.hibernate.HibernateUtil;
import dto.BonusPointDTO;
import dto.MajorCombinationDTO;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import utils.excel.BonusPointExcelImportUtil;
import java.util.List;
import java.util.Locale;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.Map;
import java.util.HashMap;

public class BonusPointService {
	private static final int PAGE_SIZE = 20;
	private final BonusPointDAO dao = new BonusPointDAO();
	private final MajorCombinationDAO majorCombinationDao = new MajorCombinationDAO();

	public int countPages(String cccdKeyword, String methodKeyword) throws SQLException {
		int totalRows = dao.countRows(cccdKeyword, methodKeyword);
		return Math.max(1, (totalRows + PAGE_SIZE - 1) / PAGE_SIZE);
	}

	public int countRows(String cccdKeyword, String methodKeyword) throws SQLException {
		return dao.countRows(cccdKeyword, methodKeyword);
	}

	public List<BonusPointDTO> getRows(String cccdKeyword, String methodKeyword, int page) throws SQLException {
		return dao.findRows(cccdKeyword, methodKeyword, page, PAGE_SIZE);
	}

	public boolean create(BonusPointDTO dto) throws SQLException {
		return dao.create(dto);
	}

	public boolean update(BonusPointDTO dto) throws SQLException {
		return dao.update(dto);
	}

	public boolean deleteById(int id) throws SQLException {
		return dao.delete(id);
	}

	public boolean upsertByKey(BonusPointDTO dto) throws SQLException {
		return dao.upsertByKey(dto);
	}

	public EnglishCertificateImportResult importEnglishCertificateScores(File file) throws SQLException, IOException {
		List<BonusPointDTO> imported = BonusPointExcelImportUtil.importEnglishCertificateRows(file);
		int totalRows = imported.size();
		int updatedRows = 0;
		int notFoundRows = 0;
		int skippedRows = 0;
		int errorRows = 0;

		for (BonusPointDTO importedRow : imported) {
			String cccd = importedRow.getTsCccd();
			Double importedScore = importedRow.getDiemCC();
			if (cccd == null || cccd.trim().isEmpty() || importedScore == null) {
				skippedRows++;
				continue;
			}

			List<BonusPointDTO> existingRows = dao.findRowsByCccd(cccd);
			if (existingRows.isEmpty()) {
				notFoundRows++;
				continue;
			}

			for (BonusPointDTO existing : existingRows) {
				try {
					boolean hasEnglish = majorCombinationDao.hasEnglishSubject(existing.getMaToHop());
					boolean isDgnl = normalize(existing.getPhuongThuc()).equals("dgnl");
					Double newDiemCC = importedScore;
					if (hasEnglish && !isDgnl) {
						newDiemCC = 0.0;
					}
					existing.setDiemCC(newDiemCC);
					Double diemUtxt = existing.getDiemUtxt() == null ? 0.0 : existing.getDiemUtxt();
					Double tong = newDiemCC + diemUtxt;
					if (tong > 3.0) {
						tong = 3.0;
					}
					existing.setDiemTong(tong);
					if (dao.update(existing)) {
						updatedRows++;
					} else {
						errorRows++;
					}
				} catch (Exception ex) {
					errorRows++;
				}
			}
		}

		return new EnglishCertificateImportResult(totalRows, updatedRows, notFoundRows, skippedRows, errorRows);
	}

	public static class EnglishCertificateImportResult {
		private final int totalRows;
		private final int updatedRows;
		private final int notFoundRows;
		private final int skippedRows;
		private final int errorRows;

		public EnglishCertificateImportResult(int totalRows, int updatedRows, int notFoundRows, int skippedRows, int errorRows) {
			this.totalRows = totalRows;
			this.updatedRows = updatedRows;
			this.notFoundRows = notFoundRows;
			this.skippedRows = skippedRows;
			this.errorRows = errorRows;
		}

		public int getTotalRows() {
			return totalRows;
		}

		public int getUpdatedRows() {
			return updatedRows;
		}

		public int getNotFoundRows() {
			return notFoundRows;
		}

		public int getSkippedRows() {
			return skippedRows;
		}

		public int getErrorRows() {
			return errorRows;
		}
	}

	// public GenerationResult generateBonusPointsFromAspirations(boolean replaceExisting) throws SQLException {
	// 	try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	// 		Transaction tx = session.beginTransaction();
	// 		try {
	// 			// Xóa dữ liệu hiện có nếu cần
	// 			int deletedExisting = 0;
	// 			if (replaceExisting) {
	// 				deletedExisting = session.createNativeMutationQuery("DELETE FROM xt_diemcongxetuyen")
	// 						.executeUpdate();
	// 			}

	// 			// Lấy tất cả nguyện vọng từ xt_nguyen_vong
	// 			@SuppressWarnings("unchecked")
	// 			List<Object[]> aspirations = session.createNativeQuery("""
	// 					SELECT nv.cccd, nv.maxettuyen, nv.matruong
	// 					FROM xt_nguyen_vong nv
	// 					ORDER BY nv.cccd ASC, nv.thutunv ASC
	// 					""").list();

	// 			int generatedCount = 0;
	// 			int skippedCount = 0;

	// 			for (Object[] aspiration : aspirations) {
	// 				String cccd = toString(aspiration[0]);
	// 				String maNganh = toString(aspiration[1]);
	// 				String maTruong = toString(aspiration[2]);
	// 				System.out.println("[BP GEN] Processing aspiration: cccd='" + cccd + "', maNganh='" + maNganh + "', maTruong='" + maTruong + "'");

	// 				if (cccd.isEmpty() || maNganh.isEmpty()) {
	// 					skippedCount++;
	// 					continue;
	// 				}

	// 				// Lấy tất cả tổ hợp của ngành từ xt_nganh_tohop
	// 				List<MajorCombinationDTO> combos = getMajorCombinationsByMajor(session, maNganh);
	// 				System.out.println("[BP GEN]   combos for " + maNganh + " -> " + combos.size());
	// 				if (combos.isEmpty()) {
	// 					skippedCount++;
	// 					continue;
	// 				}

	// 				// Với mỗi tổ hợp, kiểm tra thí sinh có đủ môn
	// 				for (MajorCombinationDTO combo : combos) {
	// 					String mon1 = combo.getMon1();
	// 					String mon2 = combo.getMon2();
	// 					String mon3 = combo.getMon3();

	// 					// Kiểm tra thí sinh có tất cả các môn của tổ hợp
	// 					if (!hasAllSubjects(session, cccd, mon1, mon2, mon3)) {
	// 						List<String> missing = getMissingSubjects(session, cccd, mon1, mon2, mon3);
	// 						System.out.println("[BP GEN]   skipped combo " + combo.getMaToHop() + " (missing: " + String.join(",", missing) + ")");
	// 						continue; // Bỏ qua tổ hợp này
	// 					}

	// 					// Tạo BonusPointDTO mà không gán điểm cộng/ưu tiên ban đầu
	// 					BonusPointDTO dto = new BonusPointDTO();
	// 					dto.setTsCccd(cccd);
	// 					dto.setPhuongThuc(maTruong); // Sử dụng maTruong làm phương thức
	// 					dto.setMaNganh(maNganh);
	// 					dto.setMaToHop(combo.getMaToHop());
	// 					dto.setDiemCC(null);
	// 					dto.setDiemUtxt(null);
	// 					dto.setDiemTong(0.0);
	// 					dto.setGhiChu("Tự động sinh từ nguyện vọng");

	// 			tx.commit();
	// 			return new GenerationResult(
	// 					replaceExisting,
	// 					aspirations.size(),
	// 					generatedCount,
	// 					skippedCount,
	// 					deletedExisting,
	// 					"Sinh dữ liệu điểm cộng thành công: " + generatedCount + " record được tạo từ " + aspirations.size() + " nguyện vọng"
	// 			);
	// 				}
	// 		} catch (Exception ex) {
	// 			if (tx != null && tx.isActive()) {
	// 				tx.rollback();
	// 			}
	// 			throw new SQLException("Lỗi sinh dữ liệu điểm cộng: " + ex.getMessage(), ex);
	// 		}
	// 	}
	// }

	public GenerationResult generateBonusPointsFromAspirations(boolean replaceExisting) throws SQLException {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			int deletedExisting = 0;
			if (replaceExisting) {
				Transaction deleteTx = null;
				try {
					deleteTx = session.beginTransaction();
					deletedExisting = session
							.createNativeMutationQuery("DELETE FROM xt_diemcongxetuyen")
							.executeUpdate();
					deleteTx.commit();
				} catch (Exception ex) {
					if (deleteTx != null && deleteTx.isActive()) {
						deleteTx.rollback();
					}
					throw new SQLException("Lỗi xóa dữ liệu điểm cộng cũ: " + ex.getMessage(), ex);
				}
			}

			@SuppressWarnings("unchecked")
			List<Object[]> aspirations = session.createNativeQuery("""
				SELECT nv.cccd, nv.maxettuyen, nv.matruong
				FROM xt_nguyen_vong nv
				ORDER BY nv.id ASC
			""").list();

			int generatedCount = 0;
			int skippedCount = 0;

			for (Object[] aspiration : aspirations) {

				String cccd = toString(aspiration[0]);
				String maNganh = toString(aspiration[1]);
				String maTruong = toString(aspiration[2]);

				System.out.println(
					"[BP GEN] Processing aspiration: cccd='"
						+ cccd
						+ "', maNganh='"
						+ maNganh
						+ "', maTruong='"
						+ maTruong
						+ "'"
				);

				if (cccd.isEmpty() || maNganh.isEmpty()) {
					skippedCount++;
					continue;
				}

				List<MajorCombinationDTO> combos = getMajorCombinationsByMajor(session, maNganh);
				System.out.println("[BP GEN] combos for " + maNganh + " -> " + combos.size());
				if (combos.isEmpty()) {
					skippedCount++;
					continue;
				}

				for (MajorCombinationDTO combo : combos) {
					String mon1 = combo.getMon1();
					String mon2 = combo.getMon2();
					String mon3 = combo.getMon3();

					if (!hasAllSubjects(session, cccd, mon1, mon2, mon3)) {
						List<String> missing = getMissingSubjects(session, cccd, mon1, mon2, mon3);
						System.out.println("[BP GEN] skipped combo " + combo.getMaToHop() + " (missing: " + String.join(",", missing) + ")");
						continue;
					}

					BonusPointDTO dto = new BonusPointDTO();
					dto.setTsCccd(cccd);
					String phuongThucExam = getExamPhuongThuc(session, cccd);
					dto.setPhuongThuc(phuongThucExam == null || phuongThucExam.isEmpty() ? maTruong : phuongThucExam);
					dto.setMaNganh(maNganh);
					dto.setMaToHop(combo.getMaToHop());
					dto.setDiemCC(null);
					dto.setDiemUtxt(null);
					dto.setDiemTong(0.0);
					dto.setGhiChu("Tự động sinh từ nguyện vọng");

					try {
						if (dao.create(dto)) {
							generatedCount++;
							System.out.println("[BP GEN] created bp for " + cccd + " - " + combo.getMaToHop());
						}
					} catch (Exception ex) {
						System.out.println("[BP GEN] failed to save bp for " + cccd + " - " + combo.getMaToHop() + ": " + ex.getMessage());
						ex.printStackTrace();
					}
				}
			}

			return new GenerationResult(
				replaceExisting,
				aspirations.size(),
				generatedCount,
				skippedCount,
				deletedExisting,
				"Sinh dữ liệu điểm cộng thành công: " + generatedCount + " record được tạo từ " + aspirations.size() + " nguyện vọng"
			);
		} catch (Exception ex) {
			throw new SQLException("Lỗi sinh dữ liệu điểm cộng: " + ex.getMessage(), ex);
		}
	}
	/**
	 * Lấy tất cả tổ hợp của ngành
	 */
	private List<MajorCombinationDTO> getMajorCombinationsByMajor(Session session, String maNganh) {
		String normalized = normalize(maNganh);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = session.createNativeQuery("""
				SELECT manganh, matohop, th_mon1, th_mon2, th_mon3
				FROM xt_nganh_tohop
				WHERE LOWER(REPLACE(REPLACE(REPLACE(manganh, ' ', ''), '_', ''), '*', '')) = LOWER(REPLACE(REPLACE(REPLACE(?1, ' ', ''), '_', ''), '*', ''))
				""").setParameter(1, normalized).list();

		List<MajorCombinationDTO> result = new ArrayList<>();
		for (Object[] row : rows) {
			MajorCombinationDTO dto = new MajorCombinationDTO();
			dto.setManganh(toString(row[0]));
			dto.setMaToHop(toString(row[1]));
			dto.setMon1(toString(row[2]));
			dto.setMon2(toString(row[3]));
			dto.setMon3(toString(row[4]));
			result.add(dto);
		}
		return result;
	}

	/**
	 * Kiểm tra thí sinh có tất cả các môn của tổ hợp không
	 */
	// private boolean hasAllSubjects(Session session, String cccd, String mon1, String mon2, String mon3) {
	// 	String normalizedCccd = normalize(cccd);
	// 	String normalizedMon1 = normalize(mon1);
	// 	String normalizedMon2 = normalize(mon2);
	// 	String normalizedMon3 = normalize(mon3);

	// 	@SuppressWarnings("unchecked")
	// 	List<Object[]> scores = session.createNativeQuery("""
	// 			SELECT LOWER(REPLACE(REPLACE(monthixettuyen, ' ', ''), '_', '')) AS subject
	// 			FROM xt_diemthixettuyen
	// 			WHERE LOWER(REPLACE(REPLACE(ts_cccd, ' ', ''), '_', '')) = LOWER(REPLACE(REPLACE(?1, ' ', ''), '_', ''))
	// 			""").setParameter(1, normalizedCccd).list();

	// 	List<String> candidateSubjects = new ArrayList<>();
	// 	for (Object[] score : scores) {
	// 		candidateSubjects.add(toString(score[0]).toLowerCase(Locale.ROOT));
	// 	}

	// 	// Kiểm tra tất cả 3 môn có trong danh sách của thí sinh không
	// 	boolean hasMon1 = candidateSubjects.contains(normalizedMon1.toLowerCase(Locale.ROOT));
	// 	boolean hasMon2 = candidateSubjects.contains(normalizedMon2.toLowerCase(Locale.ROOT));
	// 	boolean hasMon3 = candidateSubjects.contains(normalizedMon3.toLowerCase(Locale.ROOT));

	// 	return hasMon1 && hasMon2 && hasMon3;
	// }

	/**
	 * Kiểm tra thí sinh có đủ môn của tổ hợp không
	 */
	private boolean hasAllSubjects(
		Session session,
		String cccd,
		String mon1,
		String mon2,
		String mon3
	) {

		String normalizedCccd = normalize(cccd);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = session.createNativeQuery("""
			SELECT
				xt.TO, xt.LI, xt.HO, xt.SI, xt.SU, xt.DI, xt.VA,
				xt.N1_CC, xt.CNCN, xt.CNNN, xt.TI, xt.KTPL
			FROM xt_diemthixettuyen xt
			WHERE LOWER(REPLACE(REPLACE(REPLACE(xt.cccd, ' ', ''), '_', ''), '*', ''))
				= LOWER(REPLACE(REPLACE(REPLACE(:cccdParam, ' ', ''), '_', ''), '*', ''))
		""")
			.setParameter("cccdParam", normalizedCccd)
			.list();

		if (rows == null || rows.isEmpty()) {
			return false;
		}

		Object[] row = rows.get(0);
		Map<String, BigDecimal> subjectMap = new HashMap<>();
		subjectMap.put("TO", toBigDecimal(row[0]));
		subjectMap.put("LI", toBigDecimal(row[1]));
		subjectMap.put("HO", toBigDecimal(row[2]));
		subjectMap.put("SI", toBigDecimal(row[3]));
		subjectMap.put("SU", toBigDecimal(row[4]));
		subjectMap.put("DI", toBigDecimal(row[5]));
		subjectMap.put("VA", toBigDecimal(row[6]));
		subjectMap.put("N1", toBigDecimal(row[7]));
		subjectMap.put("CNCN", toBigDecimal(row[8]));
		subjectMap.put("CNNN", toBigDecimal(row[9]));
		subjectMap.put("TI", toBigDecimal(row[10]));
		subjectMap.put("KTPL", toBigDecimal(row[11]));

		return hasSubject(subjectMap, mon1)
			&& hasSubject(subjectMap, mon2)
			&& hasSubject(subjectMap, mon3);
	}

	/**
	 * Kiểm tra môn có điểm không
	 */
	private boolean hasSubject(
		Map<String, BigDecimal> subjectMap,
		String subject
	) {
		if (subject == null || subject.trim().isEmpty()) {
			return false;
		}

		String normalized = normalize(subject);
		if (normalized.equals("N1THI") || normalized.equals("N1CC") || normalized.equals("N1_CC")) {
			normalized = "N1";
		}

		BigDecimal score = subjectMap.get(normalized);
		return score != null && score.compareTo(BigDecimal.ZERO) > 0;
	}

	/**
	 * Trả về danh sách môn thiếu (chuẩn hoá) cho tổ hợp, dùng để debug
	 */
	private List<String> getMissingSubjects(Session session, String cccd, String mon1, String mon2, String mon3) {
		List<String> missing = new ArrayList<>();
		Map<String, BigDecimal> subjectMap = loadSubjectMap(session, cccd);
		if (subjectMap == null) {
			missing.add("ALL");
			return missing;
		}
		if (!hasSubject(subjectMap, mon1)) missing.add(mon1 == null ? "" : mon1);
		if (!hasSubject(subjectMap, mon2)) missing.add(mon2 == null ? "" : mon2);
		if (!hasSubject(subjectMap, mon3)) missing.add(mon3 == null ? "" : mon3);
		return missing;
	}

	private Map<String, BigDecimal> loadSubjectMap(Session session, String cccd) {
		String normalizedCccd = normalize(cccd);
		@SuppressWarnings("unchecked")
		List<Object[]> rows = session.createNativeQuery("""
			SELECT
				xt.TO, xt.LI, xt.HO, xt.SI, xt.SU, xt.DI, xt.VA,
				xt.N1_CC, xt.CNCN, xt.CNNN, xt.TI, xt.KTPL
			FROM xt_diemthixettuyen xt
			WHERE LOWER(REPLACE(REPLACE(REPLACE(xt.cccd, ' ', ''), '_', ''), '*', ''))
				= LOWER(REPLACE(REPLACE(REPLACE(?1, ' ', ''), '_', ''), '*', ''))
		""")
			.setParameter(1, normalizedCccd)
			.list();
		if (rows == null || rows.isEmpty()) return null;
		Object[] row = rows.get(0);
		Map<String, BigDecimal> subjectMap = new HashMap<>();
		subjectMap.put("TO", toBigDecimal(row[0]));
		subjectMap.put("LI", toBigDecimal(row[1]));
		subjectMap.put("HO", toBigDecimal(row[2]));
		subjectMap.put("SI", toBigDecimal(row[3]));
		subjectMap.put("SU", toBigDecimal(row[4]));
		subjectMap.put("DI", toBigDecimal(row[5]));
		subjectMap.put("VA", toBigDecimal(row[6]));
		subjectMap.put("N1", toBigDecimal(row[7]));
		subjectMap.put("CNCN", toBigDecimal(row[8]));
		subjectMap.put("CNNN", toBigDecimal(row[9]));
		subjectMap.put("TI", toBigDecimal(row[10]));
		subjectMap.put("KTPL", toBigDecimal(row[11]));
		return subjectMap;
	}

	/**
	 * Lấy phuong thuc thi của thí sinh từ xt_diemthixettuyen
	 */
	private String getExamPhuongThuc(Session session, String cccd) {
		String normalizedCccd = normalize(cccd);
		@SuppressWarnings("unchecked")
		List<Object> rows = session.createNativeQuery("""
			SELECT tx.d_phuongthuc
			FROM xt_diemthixettuyen tx
			WHERE LOWER(REPLACE(REPLACE(REPLACE(tx.cccd, ' ', ''), '_', ''), '*', ''))
				= LOWER(REPLACE(REPLACE(REPLACE(?1, ' ', ''), '_', ''), '*', ''))
		""")
			.setParameter(1, normalizedCccd)
			.list();

		if (rows == null || rows.isEmpty()) return "";
		return toString(rows.get(0));
	}


	/**
	 * Tính điểm ưu tiên từ xt_uutien_xettuyen
	 * Nếu tổ hợp có môn trùng với ma_mon thì lấy diem_cong_mondatmc, ngược lại lấy diem_cong_khongmondatmc
	 */
	private BigDecimal calculateDiemUtxt(Session session, String cccd, String mon1, String mon2, String mon3) {
		String normalizedCccd = normalize(cccd);
		String normalizedMon1 = normalize(mon1);
		String normalizedMon2 = normalize(mon2);
		String normalizedMon3 = normalize(mon3);

		// Lấy danh sách ưu tiên của thí sinh
		@SuppressWarnings("unchecked")
		List<Object[]> priorities = session.createNativeQuery("""
				SELECT ma_mon, diem_cong_mondatmc, diem_cong_khongmondatmc
				FROM xt_uutien_xettuyen
				WHERE LOWER(REPLACE(REPLACE(ts_cccd, ' ', ''), '_', '')) = LOWER(REPLACE(REPLACE(?1, ' ', ''), '_', ''))
				""").setParameter(1, normalizedCccd).list();

		BigDecimal totalDiem = BigDecimal.ZERO;

		for (Object[] priority : priorities) {
			String maMon = toString(priority[0]);
			BigDecimal diemMonDat = toBigDecimal(priority[1]);
			BigDecimal diemKhongMonDat = toBigDecimal(priority[2]);

			// Kiểm tra xem maMon có trong tổ hợp không
			String normalizedMaMon = normalize(maMon).toLowerCase(Locale.ROOT);
			boolean monInCombo = normalizedMaMon.equals(normalizedMon1.toLowerCase(Locale.ROOT))
					|| normalizedMaMon.equals(normalizedMon2.toLowerCase(Locale.ROOT))
					|| normalizedMaMon.equals(normalizedMon3.toLowerCase(Locale.ROOT));

			if (monInCombo) {
				if (diemMonDat != null) {
					totalDiem = totalDiem.add(diemMonDat);
				}
			} else {
				if (diemKhongMonDat != null) {
					totalDiem = totalDiem.add(diemKhongMonDat);
				}
			}
		}

		return totalDiem.doubleValue() > 0 ? totalDiem : BigDecimal.ZERO;
	}

	private String normalize(String value) {
		if (value == null) {
			return "";
		}
		return value.trim().replaceAll("\\s+", "").replaceAll("_", "").toUpperCase(Locale.ROOT);
	}

	private String toString(Object obj) {
		return obj == null ? "" : obj.toString().trim();
	}

	private BigDecimal toBigDecimal(Object obj) {
		if (obj == null) {
			return BigDecimal.ZERO;
		}
		try {
			if (obj instanceof BigDecimal bd) {
				return bd;
			}
			if (obj instanceof Number num) {
				return new BigDecimal(num.doubleValue());
			}
			return new BigDecimal(obj.toString().trim().replace(',', '.'));
		} catch (Exception ex) {
			return BigDecimal.ZERO;
		}
	}

	public static class GenerationResult {
		private final boolean replaceExisting;
		private final int totalAspirations;
		private final int generated;
		private final int skipped;
		private final int deletedExisting;
		private final String message;

		public GenerationResult(boolean replaceExisting, int totalAspirations, int generated, int skipped, int deletedExisting, String message) {
			this.replaceExisting = replaceExisting;
			this.totalAspirations = totalAspirations;
			this.generated = generated;
			this.skipped = skipped;
			this.deletedExisting = deletedExisting;
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}
}