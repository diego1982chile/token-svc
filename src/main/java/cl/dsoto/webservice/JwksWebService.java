package cl.dsoto.webservice;

import java.io.IOException;
import java.util.Map;

public interface JwksWebService {

    Map<String, Object> getJwks() throws IOException;
}
