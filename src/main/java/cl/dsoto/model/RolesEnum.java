package cl.dsoto.model;

/**
 * Created by root on 09-12-22.
 */
public enum RolesEnum {
    ADMIN("admin"),
    USER("user");

    private String role;

    public String getRole() {
        return this.role;
    }

    RolesEnum(String role) {
        this.role = role;
    }
}
