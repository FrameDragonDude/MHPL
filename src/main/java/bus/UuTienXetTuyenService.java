package bus;

import dal.dao.UuTienXetTuyenDAO;
import dal.entities.UuTienXetTuyenEntity;
import dto.UuTienXetTuyenDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UuTienXetTuyenService {
    private static final int PAGE_SIZE = 20;
    private final UuTienXetTuyenDAO dao;

    public UuTienXetTuyenService() {
        this.dao = new UuTienXetTuyenDAO();
    }

    public int countPages(String searchCccd, String searchGiai) throws SQLException {
        int count = dao.countRows(searchCccd, searchGiai);
        return (int) Math.ceil((double) count / PAGE_SIZE);
    }

    public int countRows(String searchCccd, String searchGiai) throws SQLException {
        return dao.countRows(searchCccd, searchGiai);
    }

    public List<UuTienXetTuyenDTO> getRows(String searchCccd, String searchGiai, int page) throws SQLException {
        List<UuTienXetTuyenEntity> entities = dao.findByPage(searchCccd, searchGiai, page);
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    public boolean create(UuTienXetTuyenDTO dto) throws SQLException {
        UuTienXetTuyenEntity entity = dtoToEntity(dto);
        return dao.create(entity);
    }

    public boolean update(UuTienXetTuyenDTO dto) throws SQLException {
        UuTienXetTuyenEntity entity = dtoToEntity(dto);
        return dao.update(entity);
    }

    public boolean deleteById(Integer id) throws SQLException {
        return dao.delete(id);
    }

    public boolean upsertByKey(UuTienXetTuyenDTO dto) throws SQLException {
        UuTienXetTuyenEntity entity = dtoToEntity(dto);
        return dao.upsertByKey(entity);
    }

    public boolean deleteAll() throws SQLException {
        return dao.deleteAll();
    }

    private UuTienXetTuyenDTO entityToDto(UuTienXetTuyenEntity entity) {
        UuTienXetTuyenDTO dto = new UuTienXetTuyenDTO();
        dto.setIdUtxt(entity.getIdUtxt());
        dto.setTsCccd(entity.getTsCccd());
        dto.setCapQuocGia(entity.getCapQuocGia());
        dto.setDoiTuyen(entity.getDoiTuyen());
        dto.setMaMon(entity.getMaMon());
        dto.setLoaiGiai(entity.getLoaiGiai());
        dto.setDiemCongMonDatMc(entity.getDiemCongMonDatMc());
        dto.setDiemCongKhongMonDatMc(entity.getDiemCongKhongMonDatMc());
        dto.setCoChungChi(entity.getCoChungChi());
        dto.setGhiChu(entity.getGhiChu());
        dto.setUtxtKeys(entity.getUtxtKeys());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private UuTienXetTuyenEntity dtoToEntity(UuTienXetTuyenDTO dto) {
        UuTienXetTuyenEntity entity = new UuTienXetTuyenEntity();
        entity.setIdUtxt(dto.getIdUtxt());
        entity.setTsCccd(dto.getTsCccd());
        entity.setCapQuocGia(dto.getCapQuocGia());
        entity.setDoiTuyen(dto.getDoiTuyen());
        entity.setMaMon(dto.getMaMon());
        entity.setLoaiGiai(dto.getLoaiGiai());
        entity.setDiemCongMonDatMc(dto.getDiemCongMonDatMc());
        entity.setDiemCongKhongMonDatMc(dto.getDiemCongKhongMonDatMc());
        entity.setCoChungChi(dto.getCoChungChi());
        entity.setGhiChu(dto.getGhiChu());
        entity.setUtxtKeys(dto.getUtxtKeys());
        entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }
}
