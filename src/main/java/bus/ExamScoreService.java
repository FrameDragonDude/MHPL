package bus;

import dal.dao.ExamScoreDAO;
import dto.ExamScoreDTO;

import java.sql.SQLException;
import java.util.List;

public class ExamScoreService {

    public static final int PAGE_SIZE = 20;

    private final ExamScoreDAO examScoreDAO;

    public ExamScoreService() {
        this.examScoreDAO = new ExamScoreDAO();
    }

    public List<ExamScoreDTO> getExamScores(String keyword, int page) throws SQLException {
        int safePage = Math.max(page, 1);
        return examScoreDAO.findExamScores(keyword, safePage, PAGE_SIZE);
    }

    public int countPages(String keyword) throws SQLException {
        int totalRows = examScoreDAO.countExamScores(keyword);
        if (totalRows == 0) {
            return 1;
        }
        return (totalRows + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    public int countRows(String keyword) throws SQLException {
        return examScoreDAO.countExamScores(keyword);
    }
}