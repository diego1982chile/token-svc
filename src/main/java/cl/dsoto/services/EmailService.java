package cl.dsoto.services;

public interface EmailService {

    void sendEmailConfirmation(String email, String confirmationUrl);
}
