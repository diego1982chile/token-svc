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
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

@RequestScoped
public class DefaultConfigService implements ConfigService {

    @ConfigProperty(name = "security.private.key")
    String privateKey;

    @Override
    public PrivateKey getPrivateKey() throws IOException {
        return getKeyPair().getPrivate();
    }

    @Override
    public PublicKey getPublicKey() throws IOException {
        return getKeyPair().getPublic();
    }

    private KeyPair getKeyPair() throws IOException {
        InputStream inputStream = DefaultConfigService.class.getResourceAsStream(privateKey);
        if (inputStream == null) {
            throw new IOException("Private key resource not found: " + privateKey);
        }

        try (inputStream;
             PEMParser pemParser = new PEMParser(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            Object object = pemParser.readObject();
            if (!(object instanceof PEMKeyPair)) {
                String type = object == null ? "empty PEM" : object.getClass().getName();
                throw new IOException("Unsupported private key format: " + type);
            }

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
                    .setProvider(new BouncyCastleProvider());
            return converter.getKeyPair((PEMKeyPair) object);
        }
    }
}
