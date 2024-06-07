package cl.dsoto.services;

import java.io.IOException;
import java.security.PrivateKey;

public interface ConfigService {

    public PrivateKey getPrivateKey() throws IOException;
}
