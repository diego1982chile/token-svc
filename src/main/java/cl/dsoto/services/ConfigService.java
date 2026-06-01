package cl.dsoto.services;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface ConfigService {

    PrivateKey getPrivateKey() throws IOException;

    PublicKey getPublicKey() throws IOException;
}
