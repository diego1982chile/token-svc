package cl.dsoto.entities;

import cl.dsoto.model.UserStatus;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by root on 09-12-22.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@UserDefinition
@Table(name = "USERS")
public class UserEntity {

    /*
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    */

    @Id
    @Username
    private String username;

    @Password
    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;


    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.DETACH})
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_username"), inverseJoinColumns = @JoinColumn(name = "role_rolename"))
    @Roles
    private Set<RoleEntity> roles;

    public String getId() {
        return username;
    }

    public Set<RoleEntity> getRoles() {
        if (this.roles != null) {
            return new HashSet<>(this.roles);
        }
        return new HashSet<>();
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }
}
