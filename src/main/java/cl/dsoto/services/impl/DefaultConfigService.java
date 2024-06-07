package cl.dsoto.services.impl;

import cl.dsoto.services.ConfigService;
import jakarta.enterprise.context.RequestScoped;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.PrivateKey;

@RequestScoped
public class DefaultConfigService implements ConfigService {

    @ConfigProperty(name = "security.private.key")
    String privateKey;

    @Override
    public PrivateKey getPrivateKey() throws IOException {
        InputStream inputStream = DefaultCypherService.class.getResourceAsStream(privateKey);

        PEMParser pemParser = new PEMParser(new InputStreamReader(inputStream));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(new BouncyCastleProvider());
        Object object = pemParser.readObject();
        KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
        return kp.getPrivate();
    }
}
