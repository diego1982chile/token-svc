package cl.dsoto.model;

/**
 * Created by root on 09-12-22.
 */
public enum RolesEnum {
    ADMIN("ADMIN"),
    USER("USER");

    private String role;

    public String getRole() {
        return this.role;
    }

    RolesEnum(String role) {
        this.role = role;
    }
}
