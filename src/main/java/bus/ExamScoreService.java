package bus;

import dal.dao.ExamScoreDAO;
import dto.ExamScoreDTO;
import dto.StatisticDTO;
import dto.MethodStatDTO;
import dto.ScoreStatDTO;

import java.sql.SQLException;
import java.util.ArrayList;
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

    public List<StatisticDTO> statisticByMethod() throws SQLException {
        List<MethodStatDTO> raw = examScoreDAO.statByMethod();

        List<StatisticDTO> result = new ArrayList<>();
        long total = 0;

        for (MethodStatDTO row : raw) {
            total += row.getSoLuong();
        }

        for (MethodStatDTO row : raw) {
            String method = row.getPhuongThuc();
            long count = row.getSoLuong();

            StatisticDTO dto = new StatisticDTO(method, count);
            dto.setPercent(total == 0 ? 0 : (count * 100.0 / total));

            result.add(dto);
        }

        return result;
    }

    public List<StatisticDTO> statisticBySubjectRange(String subject) throws SQLException {
        List<ScoreStatDTO> scores = examScoreDAO.statBySubject(subject);

        long[] buckets = new long[5];
        long total = 0;

        for (ScoreStatDTO s : scores) {
            if (s == null || s.getDiem() == null) continue;

            double diem = s.getDiem();
            long count = s.getSoLuong();

            total += count;

            if (diem < 2) buckets[0] += count;
            else if (diem < 4) buckets[1] += count;
            else if (diem < 6) buckets[2] += count;
            else if (diem < 8) buckets[3] += count;
            else buckets[4] += count;
        }

        String[] labels = {"0-2", "2-4", "4-6", "6-8", "8-10"};
        List<StatisticDTO> result = new ArrayList<>();

        for (int i = 0; i < buckets.length; i++) {
            StatisticDTO dto = new StatisticDTO(labels[i], buckets[i]);
            dto.setPercent(total == 0 ? 0 : buckets[i] * 100.0 / total);
            result.add(dto);
        }

        return result;
    }

}