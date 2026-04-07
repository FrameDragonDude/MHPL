package bus;

import dal.dao.CandidateDAO;
import dto.CandidateDTO;

import java.sql.SQLException;
import java.util.List;

public class CandidateService {

	public static final int PAGE_SIZE = 20;

	private final CandidateDAO candidateDAO;

	public CandidateService() {
		this.candidateDAO = new CandidateDAO();
	}

	public List<CandidateDTO> getCandidates(String cccdKeyword, String nameKeyword, int page) throws SQLException {
		int safePage = Math.max(page, 1);
		return candidateDAO.findCandidates(cccdKeyword, nameKeyword, safePage, PAGE_SIZE);
	}

	public int countPages(String cccdKeyword, String nameKeyword) throws SQLException {
		int totalRows = candidateDAO.countCandidates(cccdKeyword, nameKeyword);
		if (totalRows == 0) {
			return 1;
		}
		return (totalRows + PAGE_SIZE - 1) / PAGE_SIZE;
	}

	public int countRows(String cccdKeyword, String nameKeyword) throws SQLException {
		return candidateDAO.countCandidates(cccdKeyword, nameKeyword);
	}

	public boolean updateCandidate(CandidateDTO candidate) throws SQLException {
		if (candidate == null || candidate.getIdThisinh() <= 0) {
			return false;
		}
		if (candidate.getTen() == null || candidate.getTen().trim().isEmpty()) {
			return false;
		}
		return candidateDAO.updateCandidate(candidate);
	}

	public boolean createCandidate(CandidateDTO candidate) throws SQLException {
		if (candidate == null) {
			return false;
		}
		if (candidate.getCccd() == null || candidate.getCccd().trim().isEmpty()) {
			return false;
		}
		if (candidate.getTen() == null || candidate.getTen().trim().isEmpty()) {
			return false;
		}
		return candidateDAO.createCandidate(candidate);
	}

	public boolean createCandidateFull(CandidateDTO candidate) throws SQLException {
		if (candidate == null) {
			return false;
		}
		if (candidate.getCccd() == null || candidate.getCccd().trim().isEmpty()) {
			return false;
		}
		if (candidate.getTen() == null || candidate.getTen().trim().isEmpty()) {
			return false;
		}
		return candidateDAO.createCandidateFull(candidate);
	}

	public boolean upsertCandidateFull(CandidateDTO candidate) throws SQLException {
		if (candidate == null) {
			return false;
		}
		if (candidate.getCccd() == null || candidate.getCccd().trim().isEmpty()) {
			return false;
		}
		if (candidate.getTen() == null || candidate.getTen().trim().isEmpty()) {
			return false;
		}

		try {
			return candidateDAO.createCandidateFull(candidate);
		} catch (SQLException ex) {
			String message = ex.getMessage();
			boolean duplicate = message != null
					&& (message.contains("Duplicate entry") || message.contains("cccd_UNIQUE"));
			if (!duplicate) {
				throw ex;
			}

			Integer id = candidateDAO.findIdByCccd(candidate.getCccd());
			if (id == null || id <= 0) {
				throw ex;
			}

			candidate.setIdThisinh(id);
			return candidateDAO.updateCandidateFull(candidate);
		}
	}

	public boolean updateCandidateFull(CandidateDTO candidate) throws SQLException {
		if (candidate == null || candidate.getIdThisinh() <= 0) {
			return false;
		}
		if (candidate.getCccd() == null || candidate.getCccd().trim().isEmpty()) {
			return false;
		}
		if (candidate.getTen() == null || candidate.getTen().trim().isEmpty()) {
			return false;
		}
		return candidateDAO.updateCandidateFull(candidate);
	}

	public boolean deleteCandidateById(int idThisinh) throws SQLException {
		if (idThisinh <= 0) {
			return false;
		}
		return candidateDAO.deleteCandidateById(idThisinh);
	}
}
