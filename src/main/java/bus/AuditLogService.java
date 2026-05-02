package bus;

import dal.dao.AuditLogDAO;
import dal.entities.AuditLogEntity;
import dto.AuditLogDTO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLogService {
    private static final int PAGE_SIZE = 50;
    private final AuditLogDAO dao;

    public AuditLogService() {
        this.dao = new AuditLogDAO();
    }

    public boolean logAction(AuditLogDTO dto) throws SQLException {
        AuditLogEntity entity = dtoToEntity(dto);
        return dao.logAction(entity);
    }

    public int countPages(String username, String action, String module) throws SQLException {
        int count = dao.countRows(username, action, module);
        return (int) Math.ceil((double) count / PAGE_SIZE);
    }

    public int countRows(String username, String action, String module) throws SQLException {
        return dao.countRows(username, action, module);
    }

    public List<AuditLogDTO> getRows(String username, String action, String module, int page) throws SQLException {
        List<AuditLogEntity> entities = dao.findByPage(username, action, module, page);
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    public List<AuditLogDTO> getRowsByDateRange(LocalDateTime start, LocalDateTime end, int page) throws SQLException {
        List<AuditLogEntity> entities = dao.findByDateRange(start, end, page);
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    public List<AuditLogDTO> getAllLogs() throws SQLException {
        List<AuditLogEntity> entities = dao.getAllLogs();
        return entities.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    private AuditLogDTO entityToDto(AuditLogEntity entity) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setIdLog(entity.getIdLog());
        dto.setUsername(entity.getUsername());
        dto.setAction(entity.getAction());
        dto.setModule(entity.getModule());
        dto.setTableName(entity.getTableName());
        dto.setRecordId(entity.getRecordId());
        dto.setRecordInfo(entity.getRecordInfo());
        dto.setOldValue(entity.getOldValue());
        dto.setNewValue(entity.getNewValue());
        dto.setStatus(entity.getStatus());
        dto.setErrorMsg(entity.getErrorMsg());
        dto.setIpAddress(entity.getIpAddress());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private AuditLogEntity dtoToEntity(AuditLogDTO dto) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setIdLog(dto.getIdLog());
        entity.setUsername(dto.getUsername());
        entity.setAction(dto.getAction());
        entity.setModule(dto.getModule());
        entity.setTableName(dto.getTableName());
        entity.setRecordId(dto.getRecordId());
        entity.setRecordInfo(dto.getRecordInfo());
        entity.setOldValue(dto.getOldValue());
        entity.setNewValue(dto.getNewValue());
        entity.setStatus(dto.getStatus());
        entity.setErrorMsg(dto.getErrorMsg());
        entity.setIpAddress(dto.getIpAddress());
        entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }
}