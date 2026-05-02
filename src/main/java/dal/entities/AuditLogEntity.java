package dal.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_audit_log")
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Integer idLog;

    @Column(name = "username")
    private String username;

    @Column(name = "action")
    private String action;

    @Column(name = "module")
    private String module;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "record_id")
    private String recordId;

    @Column(name = "record_info", columnDefinition = "TEXT")
    private String recordInfo;

    @Column(name = "old_value", columnDefinition = "LONGTEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "LONGTEXT")
    private String newValue;

    @Column(name = "status")
    private String status;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AuditLogEntity() {}

    public AuditLogEntity(String username, String action, String module, String tableName, String recordId, String status) {
        this.username = username;
        this.action = action;
        this.module = module;
        this.tableName = tableName;
        this.recordId = recordId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getIdLog() { return idLog; }
    public void setIdLog(Integer idLog) { this.idLog = idLog; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getRecordInfo() { return recordInfo; }
    public void setRecordInfo(String recordInfo) { this.recordInfo = recordInfo; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
