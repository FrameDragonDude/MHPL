package dto;

import java.time.LocalDateTime;

public class RoleDTO {
    private Integer idRole;
    private String roleName;
    private String description;
    private LocalDateTime createdAt;

    public RoleDTO() {}

    public RoleDTO(Integer idRole, String roleName, String description) {
        this.idRole = idRole;
        this.roleName = roleName;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getIdRole() { return idRole; }
    public void setIdRole(Integer idRole) { this.idRole = idRole; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
