package dto;

import dal.entities.UserEntity;
import java.time.LocalDateTime;

/**
 * UserDTO - Data Transfer Object cho User
 * Dùng để transfer dữ liệu giữa các layer
 */
public class UserDTO {

    private Integer id;
    private String username;
    private String password; // Chỉ dùng khi tạo/cập nhật, không trả về từ DB
    private String fullname;
    private String role; // "ADMIN" hoặc "NHAN_VIEN"
    private LocalDateTime lastLogin;
    private Integer status; // 1 = ACTIVE, 0 = DISABLED

    // ==================== Constructors ====================
    public UserDTO() {
    }

    public UserDTO(Integer id, String username, String fullname, String role, Integer status) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.role = role;
        this.status = status;
    }

    /**
     * Convert từ UserEntity sang UserDTO (không lấy password)
     */
    public static UserDTO fromEntity(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.id = entity.getId();
        dto.username = entity.getUsername();
        dto.fullname = entity.getFullname();
        dto.role = entity.getRole() != null ? entity.getRole().name() : null;
        dto.lastLogin = entity.getLastLogin();
        dto.status = entity.getStatus();
        return dto;
    }

    /**
     * Convert từ UserDTO sang UserEntity
     */
    public UserEntity toEntity() {
        UserEntity entity = new UserEntity();
        entity.setId(this.id);
        entity.setUsername(this.username);
        entity.setFullname(this.fullname);
        if (this.role != null) {
            entity.setRole(UserEntity.UserRole.valueOf(this.role));
        }
        entity.setStatus(this.status);
        // Không set password ở đây - password được set riêng
        return entity;
    }

    // ==================== Getters & Setters ====================
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean isActive() {
        return status != null && status == 1;
    }

    public String getStatusDisplay() {
        return (status != null && status == 1) ? "Hoạt động" : "Khóa";
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullname='" + fullname + '\'' +
                ", role='" + role + '\'' +
                ", status=" + status +
                '}';
    }
}