package cl.dsoto.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ServiceClientRegistry {

    @ConfigProperty(name = "token.service-client.allowed-clients")
    List<String> allowedClients;

    @ConfigProperty(name = "token.service-client.audiences")
    List<String> defaultAudiences;

    private final Config config;

    @Inject
    public ServiceClientRegistry(Config config) {
        this.config = config;
    }

    public Optional<ServiceClient> authenticate(String clientId, String clientSecret) {
        if (clientId == null || clientId.isBlank() || !allowedClients.contains(clientId)) {
            return Optional.empty();
        }

        Optional<String> configuredSecret = config.getOptionalValue(secretProperty(clientId), String.class);
        if (configuredSecret.isEmpty()
                || configuredSecret.get().isBlank()
                || !configuredSecret.get().equals(clientSecret)) {
            return Optional.empty();
        }

        Set<String> scopes = config.getOptionalValue(scopesProperty(clientId), String.class)
                .map(this::parseCsv)
                .orElseGet(Set::of);

        List<String> audiences = config.getOptionalValue(audiencesProperty(clientId), String.class)
                .map(value -> List.copyOf(parseCsv(value)))
                .orElse(defaultAudiences);

        return Optional.of(new ServiceClient(clientId, scopes, audiences));
    }

    private Set<String> parseCsv(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toSet());
    }

    private String secretProperty(String clientId) {
        return "token.service-client.secrets." + clientId;
    }

    private String scopesProperty(String clientId) {
        return "token.service-client.scopes." + clientId;
    }

    private String audiencesProperty(String clientId) {
        return "token.service-client.audiences." + clientId;
    }

    public record ServiceClient(String clientId, Set<String> scopes, List<String> audiences) {
    }
}
