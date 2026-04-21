package dal.dao;

import dal.entities.CandidateEntity;
import dal.entities.ExamScoreEntity;
import dal.hibernate.HibernateUtil;
import dto.CandidateDTO;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CandidateDAO {

	public int countCandidates(String cccdKeyword, String nameKeyword) throws SQLException {
		String cccdFilter = safe(cccdKeyword);
		String nameFilter = safe(nameKeyword);

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Long total = session.createQuery(
					"select count(c.id) from CandidateEntity c "
							+ "where (:cccd = '' or c.cccd like :cccdLike) "
							+ "and (:name = '' or concat(coalesce(c.ho, ''), ' ', coalesce(c.ten, '')) like :nameLike)",
					Long.class
			)
					.setParameter("cccd", cccdFilter)
					.setParameter("cccdLike", "%" + cccdFilter + "%")
					.setParameter("name", nameFilter)
					.setParameter("nameLike", "%" + nameFilter + "%")
					.uniqueResult();
			return total == null ? 0 : total.intValue();
		} catch (Exception ex) {
			throw asSqlException("đếm thí sinh", ex);
		}
	}

	public Integer findIdByCccd(String cccd) throws SQLException {
		String value = safeNullable(cccd);
		if (value == null) {
			return null;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.createQuery(
					"select c.id from CandidateEntity c where c.cccd = :cccd",
					Integer.class
			)
					.setParameter("cccd", value)
					.uniqueResult();
		} catch (Exception ex) {
			throw asSqlException("tìm id theo CCCD", ex);
		}
	}

	public List<CandidateDTO> findCandidates(String cccdKeyword, String nameKeyword, int page, int pageSize)
			throws SQLException {
		String cccdFilter = safe(cccdKeyword);
		String nameFilter = safe(nameKeyword);
		int offset = (Math.max(page, 1) - 1) * Math.max(pageSize, 1);

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			List<CandidateEntity> entities = session.createQuery(
					"from CandidateEntity c "
							+ "where (:cccd = '' or c.cccd like :cccdLike) "
							+ "and (:name = '' or concat(coalesce(c.ho, ''), ' ', coalesce(c.ten, '')) like :nameLike) "
							+ "order by c.id asc",
					CandidateEntity.class
			)
					.setParameter("cccd", cccdFilter)
					.setParameter("cccdLike", "%" + cccdFilter + "%")
					.setParameter("name", nameFilter)
					.setParameter("nameLike", "%" + nameFilter + "%")
					.setFirstResult(offset)
					.setMaxResults(pageSize)
					.list();

			Map<String, ExamScoreEntity> scoreMap = new HashMap<>();
			List<String> cccds = entities.stream()
					.map(CandidateEntity::getCccd)
					.filter(cccd -> cccd != null && !cccd.trim().isEmpty())
					.collect(Collectors.toList());

			if (!cccds.isEmpty()) {
				List<ExamScoreEntity> scores = session.createQuery(
						"from ExamScoreEntity s where s.cccd in (:cccds)",
						ExamScoreEntity.class
				)
						.setParameterList("cccds", cccds)
						.list();
				for (ExamScoreEntity score : scores) {
					scoreMap.put(score.getCccd(), score);
				}
			}

			List<CandidateDTO> results = new ArrayList<>();
			for (CandidateEntity entity : entities) {
				CandidateDTO dto = mapCandidateBase(entity);
				fillDerivedFields(dto, scoreMap.get(entity.getCccd()));
				results.add(dto);
			}
			return results;
		} catch (Exception ex) {
			throw asSqlException("tải danh sách thí sinh", ex);
		}
	}

	public boolean updateCandidate(CandidateDTO candidate) throws SQLException {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			CandidateEntity entity = session.find(CandidateEntity.class, candidate.getIdThisinh());
			if (entity == null) {
				tx.rollback();
				return false;
			}
			entity.setHo(safeNullable(candidate.getHo()));
			entity.setTen(safeNullable(candidate.getTen()));
			entity.setNgaySinh(safeNullable(candidate.getNgaySinh()));
			entity.setDienThoai(safeNullable(candidate.getDienThoai()));
			entity.setGioiTinh(safeNullable(candidate.getGioiTinh()));
			entity.setEmail(safeNullable(candidate.getEmail()));
			entity.setNoiSinh(safeNullable(candidate.getNoiSinh()));
			entity.setDoiTuong(safeNullable(candidate.getDoiTuong()));
			entity.setKhuVuc(safeNullable(candidate.getKhuVuc()));
			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("cập nhật thí sinh", ex);
		}
	}

	public boolean createCandidate(CandidateDTO candidate) throws SQLException {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			CandidateEntity entity = new CandidateEntity();
			applyCandidateFromDto(entity, candidate);
			session.persist(entity);
			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("thêm thí sinh", ex);
		}
	}

	public boolean deleteCandidateById(int idThisinh) throws SQLException {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			CandidateEntity entity = session.find(CandidateEntity.class, idThisinh);
			if (entity == null) {
				tx.rollback();
				return false;
			}
			String cccd = safeNullable(entity.getCccd());
			if (cccd != null) {
				session.createMutationQuery("delete from ExamScoreEntity s where s.cccd = :cccd")
						.setParameter("cccd", cccd)
						.executeUpdate();
			}
			session.remove(entity);
			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("xóa thí sinh", ex);
		}
	}

	public boolean createCandidateFull(CandidateDTO candidate) throws SQLException {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			CandidateEntity entity = new CandidateEntity();
			applyCandidateFromDto(entity, candidate);
			session.persist(entity);

			ExamScoreEntity score = new ExamScoreEntity();
			applyScoreFromDto(score, candidate);
			score.setCccd(safeNullable(candidate.getCccd()));
			score.setSoBaoDanh(safeNullable(candidate.getSoBaoDanh()));
			String phuongThuc = safeNullable(candidate.getMaMonNn());
			score.setPhuongThuc(phuongThuc);
			session.persist(score);

			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("thêm thí sinh đầy đủ", ex);
		}
	}

	public boolean updateCandidateFull(CandidateDTO candidate) throws SQLException {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			CandidateEntity entity = session.find(CandidateEntity.class, candidate.getIdThisinh());
			if (entity == null) {
				tx.rollback();
				return false;
			}

			String oldCccd = safeNullable(entity.getCccd());
			applyCandidateFromDto(entity, candidate);

			ExamScoreEntity score = null;
			if (oldCccd != null) {
				score = session.createQuery("from ExamScoreEntity s where s.cccd = :cccd", ExamScoreEntity.class)
						.setParameter("cccd", oldCccd)
						.uniqueResult();
			}

			if (score == null) {
				score = new ExamScoreEntity();
				score.setCccd(safeNullable(candidate.getCccd()));
				score.setSoBaoDanh(safeNullable(candidate.getSoBaoDanh()));
				String phuongThuc = safeNullable(candidate.getMaMonNn());
				score.setPhuongThuc(phuongThuc);
				applyScoreFromDto(score, candidate);
				session.persist(score);
			} else {
				score.setCccd(safeNullable(candidate.getCccd()));
				score.setSoBaoDanh(safeNullable(candidate.getSoBaoDanh()));
				String phuongThuc = safeNullable(candidate.getMaMonNn());
				if (phuongThuc != null) {
					score.setPhuongThuc(phuongThuc);
				}
				applyScoreFromDto(score, candidate);
			}

			tx.commit();
			return true;
		} catch (Exception ex) {
			rollbackQuietly(tx);
			throw asSqlException("cập nhật thí sinh đầy đủ", ex);
		}
	}

	private CandidateDTO mapCandidateBase(CandidateEntity entity) {
		CandidateDTO candidate = new CandidateDTO();
		candidate.setIdThisinh(entity.getIdThisinh() == null ? 0 : entity.getIdThisinh());
		candidate.setCccd(entity.getCccd());
		candidate.setSoBaoDanh(entity.getSoBaoDanh());
		candidate.setHo(entity.getHo());
		candidate.setTen(entity.getTen());
		candidate.setNgaySinh(entity.getNgaySinh());
		candidate.setDienThoai(entity.getDienThoai());
		candidate.setGioiTinh(entity.getGioiTinh());
		candidate.setEmail(entity.getEmail());
		candidate.setNoiSinh(entity.getNoiSinh());
		candidate.setDoiTuong(entity.getDoiTuong());
		candidate.setKhuVuc(entity.getKhuVuc());
		candidate.setChuongTrinh(entity.getChuongTrinh());
		candidate.setDanToc(entity.getDanToc());
		candidate.setMaDanToc(entity.getMaDanToc());
		return candidate;
	}

	private void fillDerivedFields(CandidateDTO candidate, ExamScoreEntity score) {
		if (score != null) {
			candidate.setDiemTo(score.getDiemTo());
			candidate.setDiemVa(score.getDiemVa());
			candidate.setDiemLi(score.getDiemLi());
			candidate.setDiemHo(score.getDiemHo());
			candidate.setDiemSi(score.getDiemSi());
			candidate.setDiemSu(score.getDiemSu());
			candidate.setDiemDi(score.getDiemDi());
			candidate.setDiemNn(score.getDiemN1Cc());
			candidate.setDiemKtpl(score.getDiemKtpl());
			candidate.setDiemTi(score.getDiemTi());
			candidate.setDiemCncn(score.getDiemCncn());
			candidate.setDiemCnnn(score.getDiemCnnn());
			candidate.setDiemNk1(score.getDiemNk1());
			candidate.setDiemNk2(score.getDiemNk2());
			candidate.setMaMonNn(safeNullable(score.getPhuongThuc()));
		} else {
			candidate.setMaMonNn(null);
		}

		if (candidate.getDiemTo() != null && candidate.getDiemVa() != null && candidate.getDiemLi() != null) {
			candidate.setDiemXetTotNghiep(round2((candidate.getDiemTo() + candidate.getDiemVa() + candidate.getDiemLi()) / 3.0));
		} else {
			candidate.setDiemXetTotNghiep(null);
		}
	}

	private void applyCandidateFromDto(CandidateEntity entity, CandidateDTO candidate) {
		entity.setCccd(safeNullable(candidate.getCccd()));
		entity.setSoBaoDanh(safeNullable(candidate.getSoBaoDanh()));
		entity.setHo(safeNullable(candidate.getHo()));
		entity.setTen(safeNullable(candidate.getTen()));
		entity.setNgaySinh(safeNullable(candidate.getNgaySinh()));
		entity.setDienThoai(safeNullable(candidate.getDienThoai()));
		entity.setGioiTinh(safeNullable(candidate.getGioiTinh()));
		entity.setEmail(safeNullable(candidate.getEmail()));
		entity.setNoiSinh(safeNullable(candidate.getNoiSinh()));
		entity.setDoiTuong(safeNullable(candidate.getDoiTuong()));
		entity.setKhuVuc(safeNullable(candidate.getKhuVuc()));
		entity.setChuongTrinh(safeNullable(candidate.getChuongTrinh()));
		entity.setDanToc(safeNullable(candidate.getDanToc()));
		entity.setMaDanToc(safeNullable(candidate.getMaDanToc()));
	}

	private void applyScoreFromDto(ExamScoreEntity score, CandidateDTO candidate) {
		score.setDiemTo(candidate.getDiemTo());
		score.setDiemVa(candidate.getDiemVa());
		score.setDiemLi(candidate.getDiemLi());
		score.setDiemHo(candidate.getDiemHo());
		score.setDiemSi(candidate.getDiemSi());
		score.setDiemSu(candidate.getDiemSu());
		score.setDiemDi(candidate.getDiemDi());
		score.setDiemN1Cc(candidate.getDiemNn());
		score.setDiemKtpl(candidate.getDiemKtpl());
		score.setDiemTi(candidate.getDiemTi());
		score.setDiemCncn(candidate.getDiemCncn());
		score.setDiemCnnn(candidate.getDiemCnnn());
		score.setDiemNk1(candidate.getDiemNk1());
		score.setDiemNk2(candidate.getDiemNk2());
	}

	private double round2(double value) {
		return Math.round(value * 100.0) / 100.0;
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

	private SQLException asSqlException(String action, Exception ex) {
		if (ex instanceof SQLException) {
			return (SQLException) ex;
		}
		return new SQLException("Loi Hibernate khi " + action + ": " + ex.getMessage(), ex);
	}

	private void rollbackQuietly(Transaction tx) {
		if (tx != null) {
			try {
				if (tx.isActive()) {
					tx.rollback();
				}
			} catch (Exception ignored) {
				// Ignore rollback failure to avoid masking the original exception.
			}
		}
	}
}
