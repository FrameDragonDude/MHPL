package dal.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_permissions")
public class PermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permission")
    private Integer idPermission;

    @Column(name = "permission_code", nullable = false, unique = true)
    private String permissionCode;

    @Column(name = "permission_name", nullable = false)
    private String permissionName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "module")
    private String module;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PermissionEntity() {}

    public PermissionEntity(String permissionCode, String permissionName, String module, String description) {
        this.permissionCode = permissionCode;
        this.permissionName = permissionName;
        this.module = module;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getIdPermission() { return idPermission; }
    public void setIdPermission(Integer idPermission) { this.idPermission = idPermission; }

    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }

    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}