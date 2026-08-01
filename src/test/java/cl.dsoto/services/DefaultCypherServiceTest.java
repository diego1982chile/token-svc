package cl.dsoto.services;

import cl.dsoto.services.impl.DefaultCypherService;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

class DefaultCypherServiceTest {

    @Test
    void shouldGenerateAccessTokenForMultipleAudiences() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        String token = new DefaultCypherService().generateJWT(
                pair.getPrivate(),
                "user@example.com",
                List.of("USER"),
                "https://apis.internal.dsoto.cl",
                List.of("identity-svc", "onboarding-svc", "profile-service")
        );

        assertThat(
                SignedJWT.parse(token).getJWTClaimsSet().getAudience(),
                containsInAnyOrder("identity-svc", "onboarding-svc", "profile-service")
        );
    }
}
