package bus;

import dal.dao.CandidateDAO;
import dto.StatisticsDTO;

import java.sql.SQLException;

public class ScoreStatisticsService {

	private final CandidateDAO candidateDAO = new CandidateDAO();

	public StatisticsDTO getBasicStatistics() throws SQLException {
		StatisticsDTO dto = new StatisticsDTO();
		dto.setTotal(candidateDAO.totalCount());
		dto.setCountByKhuVuc(candidateDAO.countByKhuVuc());
		dto.setCountByDoiTuong(candidateDAO.countByDoiTuong());
		return dto;
	}
}