package bus;

import dal.dao.BonusPointDAO;
import dal.dao.MajorCombinationDAO;
import dal.hibernate.HibernateUtil;
import dto.BonusPointDTO;
import dto.MajorCombinationDTO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
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

	public GenerationResult generateBonusPointsFromAspirations(boolean replaceExisting) throws SQLException {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Transaction tx = session.beginTransaction();
			try {
				// Xóa dữ liệu hiện có nếu cần
				int deletedExisting = 0;
				if (replaceExisting) {
					deletedExisting = session.createNativeMutationQuery("DELETE FROM xt_diemcongxetuyen")
							.executeUpdate();
				}

				// Lấy tất cả nguyện vọng từ xt_nguyen_vong
				@SuppressWarnings("unchecked")
				List<Object[]> aspirations = session.createNativeQuery("""
						SELECT nv.cccd, nv.maxettuyen, nv.matruong
						FROM xt_nguyen_vong nv
						ORDER BY nv.cccd ASC, nv.thutunv ASC
						""").list();

				int generatedCount = 0;
				int skippedCount = 0;

				for (Object[] aspiration : aspirations) {
					String cccd = toString(aspiration[0]);
					String maNganh = toString(aspiration[1]);
					String maTruong = toString(aspiration[2]);

					if (cccd.isEmpty() || maNganh.isEmpty()) {
						skippedCount++;
						continue;
					}

					// Lấy tất cả tổ hợp của ngành từ xt_nganh_tohop
					List<MajorCombinationDTO> combos = getMajorCombinationsByMajor(session, maNganh);
					if (combos.isEmpty()) {
						skippedCount++;
						continue;
					}

					// Với mỗi tổ hợp, kiểm tra thí sinh có đủ môn
					for (MajorCombinationDTO combo : combos) {
						String mon1 = combo.getMon1();
						String mon2 = combo.getMon2();
						String mon3 = combo.getMon3();

						// Kiểm tra thí sinh có tất cả các môn của tổ hợp
						if (!hasAllSubjects(session, cccd, mon1, mon2, mon3)) {
							continue; // Bỏ qua tổ hợp này
						}

						// Tính diemUtxt từ xt_uutien_xettuyen
						BigDecimal diemUtxt = calculateDiemUtxt(session, cccd, mon1, mon2, mon3);

						// Tạo BonusPointDTO
						BonusPointDTO dto = new BonusPointDTO();
						dto.setTsCccd(cccd);
						dto.setPhuongThuc(maTruong); // Sử dụng maTruong làm phương thức
						dto.setMaNganh(maNganh);
						dto.setMaToHop(combo.getMaToHop());
						dto.setDiemCC(0.0); // Điểm chứng chỉ ngoại ngữ (mặc định 0)
						dto.setDiemUtxt(diemUtxt != null ? diemUtxt.doubleValue() : 0.0);
						dto.setDiemTong((dto.getDiemCC() != null ? dto.getDiemCC() : 0.0) + (dto.getDiemUtxt() != null ? dto.getDiemUtxt() : 0.0));
						dto.setGhiChu("Tự động sinh từ nguyện vọng");

						// Lưu vào database
						try {
							if (dao.create(dto)) {
								generatedCount++;
							}
						} catch (Exception ex) {
							// Bỏ qua lỗi cá nhân và tiếp tục
						}
					}
				}

				tx.commit();
				return new GenerationResult(
						replaceExisting,
						aspirations.size(),
						generatedCount,
						skippedCount,
						deletedExisting,
						"Sinh dữ liệu điểm cộng thành công: " + generatedCount + " record được tạo từ " + aspirations.size() + " nguyện vọng"
				);
			} catch (Exception ex) {
				if (tx != null && tx.isActive()) {
					tx.rollback();
				}
				throw new SQLException("Lỗi sinh dữ liệu điểm cộng: " + ex.getMessage(), ex);
			}
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
				WHERE LOWER(REPLACE(REPLACE(manganh, ' ', ''), '_', '')) = LOWER(REPLACE(REPLACE(?1, ' ', ''), '_', ''))
				ORDER BY matohop ASC
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

			Object[] row = session.createNativeQuery("""
				SELECT
					xt.TO, xt.LI, xt.HO, xt.SI, xt.SU, xt.DI, xt.VA, xt.N1_THI, xt.CNCN, xt.CNNN, xt.TI, xt.KTPL
				FROM xt_diemthixettuyen xt
				WHERE LOWER(REPLACE(REPLACE(xt.cccd, ' ', ''), '*', ''))
					= LOWER(REPLACE(REPLACE(:cccdParam, ' ', ''), '*', ''))
			""", Object[].class) // <-- Thêm Object[].class vào đây
			.setParameter("cccdParam", normalizedCccd) // <-- Dùng Named Parameter cho an toàn
			.uniqueResult();

			if (row == null) {
			return false;
		}

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

		String normalized = normalize(subject);

		BigDecimal score = subjectMap.get(normalized);

		return score != null
		&& score.compareTo(BigDecimal.ZERO) > 0;
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
		return value.trim().replaceAll("\\s+", "").replaceAll("_", "");
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