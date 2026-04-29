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

    public List<ExamScoreDTO> getExamScores(String keyword, String filter, int page) throws SQLException {
        int safePage = Math.max(page, 1);
        return examScoreDAO.findExamScores(keyword, filter, safePage, PAGE_SIZE);
    }

    public int countPages(String keyword, String filter) throws SQLException {
        int totalRows = examScoreDAO.countExamScores(keyword, filter);
        if (totalRows == 0) {
            return 1;
        }
        return (totalRows + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    public int countRows(String keyword, String filter) throws SQLException {
        return examScoreDAO.countExamScores(keyword, filter);
    }

    public ExamScoreDTO getExamScoreById(int id) {
        try {
            return examScoreDAO.findExamScoreById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addExamScore(ExamScoreDTO dto) {
        try {
            if(examScoreDAO.findExamScoreById(dto.getIdDiemThi()) != null) {
                return false; // ID đã tồn tại
            }
            examScoreDAO.insert(dto);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateExamScore(ExamScoreDTO dto) {
        try {
            if (dto.getIdDiemThi() <= 0) return false;
            examScoreDAO.update(dto);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteExamScore(int id) {
        try {
            if (id <= 0) return false;
            examScoreDAO.delete(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}