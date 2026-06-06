package cl.dsoto.services.impl;


import cl.dsoto.model.MPJWTToken;
import cl.dsoto.services.CypherService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.RequestScoped;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by root on 09-12-22.
 */
@RequestScoped
public class DefaultCypherService implements CypherService {

    private static final String EMAIL_CONFIRMATION_TYPE = "email-confirmation";

    @Override
    public String generateJWT(PrivateKey key, String subject, List<String> groups, String issuer, List<String> audiences) {
        MPJWTToken token = new MPJWTToken();
        token.setAud(audiences);
        token.setIss(issuer);  // Must match the expected issues configuration values
        token.setJti(UUID.randomUUID().toString());

        token.setSub(subject);
        token.setUpn(subject);

        token.setIat(System.currentTimeMillis());
        token.setExp(System.currentTimeMillis() + 60 * 60 * 1000); // 1 hour expiration!

        token.setGroups(groups);

        return signJWT(key, token);
    }

    @Override
    public String generateEmailConfirmationJWT(PrivateKey key, String email, String issuer, String audience) {
        MPJWTToken token = new MPJWTToken();
        token.setAud(List.of(audience));
        token.setIss(issuer);
        token.setJti(UUID.randomUUID().toString());
        token.setSub(email);
        token.setUpn(email);
        token.setIat(System.currentTimeMillis());
        token.setExp(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        token.setGroups(List.of());
        token.addAdditionalClaims("typ", EMAIL_CONFIRMATION_TYPE);

        return signJWT(key, token);
    }

    @Override
    public String validateEmailConfirmationJWT(String token, PublicKey key, String issuer, String audience) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Email confirmation token is required");
        }

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) key);

            if (!signedJWT.verify(verifier)) {
                throw new IllegalArgumentException("Invalid email confirmation token signature");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (!issuer.equals(claims.getIssuer())) {
                throw new IllegalArgumentException("Invalid email confirmation token issuer");
            }

            if (!claims.getAudience().contains(audience)) {
                throw new IllegalArgumentException("Invalid email confirmation token audience");
            }

            if (!EMAIL_CONFIRMATION_TYPE.equals(claims.getStringClaim("typ"))) {
                throw new IllegalArgumentException("Invalid email confirmation token type");
            }

            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                throw new IllegalArgumentException("Expired email confirmation token");
            }

            return claims.getSubject();
        } catch (JOSEException | ParseException e) {
            Log.error("Email confirmation JWT validation failed", e);
            throw new IllegalArgumentException("Invalid email confirmation token", e);
        }
    }

    private String signJWT(PrivateKey key, MPJWTToken token) {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID("apisKey")
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(token.toJSONString()));

        // Apply the Signing protection
        JWSSigner signer = new RSASSASigner(key);

        try {
            jwsObject.sign(signer);
        } catch (JOSEException e) {
            Log.error("JWT signing failed", e);
            throw new IllegalStateException("JWT signing failed", e);
        }

        return jwsObject.serialize();
    }
}
