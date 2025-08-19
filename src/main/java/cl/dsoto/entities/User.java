package cl.dsoto.entities;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.RolesValue;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.EMPTY_SET;

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
public class User {

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


    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.DETACH})
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_username"), inverseJoinColumns = @JoinColumn(name = "role_rolename"))
    @Roles
    private Set<Role> roles;

    public String getId() {
        return username;
    }

    // Asegurarse de devolver un Set modificable
    public Set<Role> getRoles() {
        if (this.roles != null) {
            return new HashSet<>(this.roles); // Devuelve un Set modificable
        } else {
            return EMPTY_SET; // Devuelve un Set modificable
        }
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
