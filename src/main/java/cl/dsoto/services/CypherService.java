package cl.dsoto.services;


import java.security.PrivateKey;
import java.util.List;

/**
 * Created by root on 09-12-22.
 */
public interface CypherService {

    String generateJWT(PrivateKey key, String subject, List<String> groups, String issuer, String audience);

    String generateEmailConfirmationJWT(PrivateKey key, String email, String issuer, String audience);

    String validateEmailConfirmationJWT(String token, java.security.PublicKey key, String issuer, String audience);

}
