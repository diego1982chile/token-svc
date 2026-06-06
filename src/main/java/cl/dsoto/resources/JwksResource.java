package cl.dsoto.resources;

import cl.dsoto.services.ConfigService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Path("/.well-known/jwks.json")
@Produces(MediaType.APPLICATION_JSON)
public class JwksResource {

    private static final String KEY_ID = "apisKey";

    @Inject
    ConfigService configService;

    @GET
    @PermitAll
    public Map<String, Object> getJwks() throws IOException {
        RSAPublicKey publicKey = (RSAPublicKey) configService.getPublicKey();
        return Map.of("keys", List.of(Map.of(
                "kty", "RSA",
                "use", "sig",
                "alg", "RS256",
                "kid", KEY_ID,
                "n", base64Url(publicKey.getModulus()),
                "e", base64Url(publicKey.getPublicExponent())
        )));
    }

    private String base64Url(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            bytes = java.util.Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
