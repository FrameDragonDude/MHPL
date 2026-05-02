package dto;

import java.time.LocalDateTime;

public class PermissionDTO {
    private Integer idPermission;
    private String permissionCode;
    private String permissionName;
    private String description;
    private String module;
    private LocalDateTime createdAt;

    public PermissionDTO() {}

    public PermissionDTO(Integer idPermission, String permissionCode, String permissionName, String module, String description) {
        this.idPermission = idPermission;
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
