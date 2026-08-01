package cl.dsoto.services;

public class ActiveUserAlreadyExistsException extends RuntimeException {

    public ActiveUserAlreadyExistsException(String username) {
        super("Active user already exists: " + username);
    }
}
