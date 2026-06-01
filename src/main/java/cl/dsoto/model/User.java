package cl.dsoto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String username;
    private String password;
    private Set<Role> roles;
    private UserStatus status;

    public String getId() {
        return username;
    }

    public Set<Role> getRoles() {
        if (roles != null) {
            return new HashSet<>(roles);
        }
        return new HashSet<>();
    }
}
