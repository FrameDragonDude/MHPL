package dal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_roles")
public class RoleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_role")
	private Integer idRole;

	@Column(name = "role_name", nullable = false, unique = true)
	private String roleName;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public RoleEntity() {}

	public Integer getIdRole() { return idRole; }
	public void setIdRole(Integer idRole) { this.idRole = idRole; }

	public String getRoleName() { return roleName; }
	public void setRoleName(String roleName) { this.roleName = roleName; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}