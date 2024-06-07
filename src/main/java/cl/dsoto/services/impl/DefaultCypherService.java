package cl.dsoto.services.impl;


import cl.dsoto.model.MPJWTToken;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.RequestScoped;

import java.security.PrivateKey;
import java.util.List;
import java.util.UUID;

/**
 * Created by root on 09-12-22.
 */
@RequestScoped
public class DefaultCypherService {

    public static String generateJWT(PrivateKey key, String subject, List<String> groups) {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID("apisKey")
                .build();

        MPJWTToken token = new MPJWTToken();
        token.setAud("apisGt");
        //token.setIss("https://apis.internal.forevision.cl");  // Must match the expected issues configuration values
        token.setIss("https://apis.internal.dsoto.cl");  // Must match the expected issues configuration values
        token.setJti(UUID.randomUUID().toString());

        token.setSub(subject);
        token.setUpn(subject);

        token.setIat(System.currentTimeMillis());
        //token.setExp(System.currentTimeMillis() + 7*24*60*60*1000); // 1 week expiration!
        token.setExp(System.currentTimeMillis() + 60 * 60 * 1000); // 1 hour expiration!

        token.setGroups(groups);

        JWSObject jwsObject = new JWSObject(header, new Payload(token.toJSONString()));

        // Apply the Signing protection
        JWSSigner signer = new RSASSASigner(key);

        try {
            jwsObject.sign(signer);
        } catch (JOSEException e) {
            Log.error(e.getMessage());
        }

        return jwsObject.serialize();
    }

}
