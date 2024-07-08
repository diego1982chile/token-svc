package cl.dsoto.config;

import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class AccessAnnotationCheckerProducer {

    @Produces
    public AccessAnnotationChecker produceAccessAnnotationChecker() {
        return new AccessAnnotationChecker();
    }
}
