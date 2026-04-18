package dal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;

/**
 * UserEntity - Map với bảng xt_staff_accounts
 * Lưu trữ tài khoản nhân viên/admin và thông tin đăng nhập
 */
@Entity
@Table(name = "xt_staff_accounts")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_staff")
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password; // Lưu dạng hash (bcrypt)

    @Column(name = "fullname", length = 100)
    private String fullname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "last_login")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastLogin;

    @Column(name = "status", nullable = false)
    private Integer status; // 1 = ACTIVE, 0 = DISABLED

    // ==================== Constructors ====================
    public UserEntity() {
    }

    public UserEntity(String username, String password, String fullname, UserRole role) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.role = role;
        this.status = 1; // ACTIVE by default
        this.lastLogin = null;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
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

    // ==================== Enum ====================
    public enum UserRole {
        ADMIN("Quản trị viên"),
        NHAN_VIEN("Nhân viên");

        private final String display;

        UserRole(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }
}