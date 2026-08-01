package cl.dsoto.services.impl;


import cl.dsoto.events.DomainEventPublisher;
import cl.dsoto.events.EmailConfirmationRequested;
import cl.dsoto.entities.IdentityEventLogEntryEntity;
import cl.dsoto.entities.RoleEntity;
import cl.dsoto.entities.UserEntity;
import cl.dsoto.mappers.UserMapper;
import cl.dsoto.model.IdentityEventType;
import cl.dsoto.model.Role;
import cl.dsoto.model.RegistrationResponse;
import cl.dsoto.model.User;
import cl.dsoto.model.UserStatus;
import cl.dsoto.repositories.IdentityEventLogEntryRepository;
import cl.dsoto.repositories.RoleRepository;
import cl.dsoto.repositories.UserRepository;
import cl.dsoto.services.ActiveUserAlreadyExistsException;
import cl.dsoto.services.ConfigService;
import cl.dsoto.services.CypherService;
import cl.dsoto.services.UserService;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Created by root on 13-10-22.
 */
@RequestScoped
public class DefaultUserService implements UserService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private IdentityEventLogEntryRepository identityEventLogEntryRepository;

    @Inject
    private UserMapper userMapper;

    @Inject
    private ConfigService configService;

    @Inject
    private CypherService cypherService;

    @Inject
    private DomainEventPublisher domainEventPublisher;

    @ConfigProperty(name = "token.issuer")
    String jwtIssuer;

    @ConfigProperty(name = "token.audience")
    String jwtAudience;

    @ConfigProperty(name = "email.confirmation.ui-url")
    Optional<String> emailConfirmationUiUrl;

    @ConfigProperty(name = "frontend.public-url")
    String frontendPublicUrl;

    @ConfigProperty(name = "email.confirmation.route")
    String emailConfirmationRoute;

    @ConfigProperty(name = "email.confirmation.ttl-hours", defaultValue = "24")
    long emailConfirmationTtlHours;

    @Override
    @Transactional
    public List<User> getAllUsers() {
        List<User> users = userMapper.toModelList(userRepository.findAllOrderByName());
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    @Transactional
    @Override
    public User saveUser(User user) {

        UserEntity previous = userRepository.findByUsername(user.getUsername());

        if(previous != null) {
            if(user.getPassword() != null) {
                previous.setPassword(BcryptUtil.bcryptHash(user.getPassword()));
            }
            previous.setRoles(userMapper.toEntity(user).getRoles());

            return userMapper.toModel(userRepository.save(previous));
        }
        else {
            UserEntity userEntity = userMapper.toEntity(user);
            userEntity.setPassword(BcryptUtil.bcryptHash(user.getPassword()));
            userEntity.setStatus(UserStatus.PENDING);

            User savedUser = userMapper.toModel(userRepository.save(userEntity));
            sendEmailConfirmation(savedUser.getUsername());

            return savedUser;
        }
    }

    @Override
    @Transactional
    public RegistrationResponse registerUser(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email and password are required");
        }

        String username = email.trim().toLowerCase();

        UserEntity previous = userRepository.findByUsername(username);
        if (previous != null) {
            if (previous.getStatus() == UserStatus.ACTIVE) {
                throw new ActiveUserAlreadyExistsException(username);
            }

            sendEmailConfirmation(previous.getUsername());
            return new RegistrationResponse(UUID.randomUUID().toString());
        }

        RoleEntity userRole = roleRepository.findByRolename("USER");
        if (userRole == null) {
            throw new IllegalStateException("USER role is not configured");
        }

        String registrationId = UUID.randomUUID().toString();
        User user = User.builder()
                .username(username)
                .password(password)
                .roles(Set.of(Role.builder()
                        .id(userRole.getId())
                        .rolename(userRole.getRolename())
                        .build()))
                .build();

        UserEntity userEntity = userMapper.toEntity(user);
        userEntity.setPassword(BcryptUtil.bcryptHash(user.getPassword()));
        userEntity.setStatus(UserStatus.PENDING);

        User savedUser = userMapper.toModel(userRepository.save(userEntity));
        appendIdentityEvent(IdentityEventType.USER_REGISTERED, savedUser.getUsername(), registrationId);
        sendEmailConfirmation(savedUser.getUsername());

        return new RegistrationResponse(registrationId);
    }

    @Transactional
    @Override
    public User confirmEmail(String token) {
        try {
            String username = cypherService.validateEmailConfirmationJWT(token, configService.getPublicKey(), jwtIssuer, jwtAudience);
            UserEntity user = userRepository.findByUsername(username);

            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }

            if (user.getStatus() == UserStatus.ACTIVE) {
                return userMapper.toModel(user);
            }

            user.setStatus(UserStatus.ACTIVE);
            UserEntity savedUser = userRepository.save(user);
            appendIdentityEvent(IdentityEventType.EMAIL_VERIFIED, user.getUsername(), null);
            return userMapper.toModel(savedUser);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load JWT public key", e);
        }
    }

    @Transactional
    @Override
    public void resendEmailConfirmation(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        UserEntity user = userRepository.findByUsername(email);

        if (user == null || user.getStatus() == UserStatus.ACTIVE) {
            return;
        }

        sendEmailConfirmation(user.getUsername());
    }

    @Override
    public boolean isUserActive(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        UserEntity user = userRepository.findByUsername(username);
        return user != null && (user.getStatus() == null || user.getStatus() == UserStatus.ACTIVE);
    }

    @Transactional
    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<User> getUser(String id) {
        return userRepository.findById(id).map(userMapper::toModel);
    }

    private void sendEmailConfirmation(String email) {
        try {
            String token = cypherService.generateEmailConfirmationJWT(configService.getPrivateKey(), email, jwtIssuer, jwtAudience);
            Instant occurredAt = Instant.now();
            EmailConfirmationRequested event = new EmailConfirmationRequested(
                    UUID.randomUUID().toString(),
                    EmailConfirmationRequested.TYPE,
                    EmailConfirmationRequested.VERSION,
                    occurredAt,
                    email,
                    email,
                    buildConfirmationUrl(token),
                    occurredAt.plusSeconds(emailConfirmationTtlHours * 60 * 60)
            );

            domainEventPublisher.publish(event);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load JWT private key", e);
        }
    }

    private void appendIdentityEvent(
            IdentityEventType eventType,
            String subject,
            String registrationId
    ) {
        identityEventLogEntryRepository.save(IdentityEventLogEntryEntity.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .subject(subject)
                .registrationId(registrationId)
                .occurredAt(Instant.now())
                .build());
    }

    private String buildConfirmationUrl(String token) {
        String confirmationUrl = emailConfirmationUiUrl
                .filter(value -> !value.isBlank())
                .orElseGet(this::defaultFrontendConfirmationUrl);

        return appendQueryParam(
                normalizeUrl(confirmationUrl),
                "confirmEmailToken",
                token
        );
    }

    private String appendQueryParam(String url, String name, String value) {
        String separator = url.contains("?") ? "&" : "?";
        return url
                + separator
                + URLEncoder.encode(name, StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String normalizeUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private String defaultFrontendConfirmationUrl() {
        String route = emailConfirmationRoute == null || emailConfirmationRoute.isBlank()
                ? "/"
                : emailConfirmationRoute;
        if (!route.startsWith("/")) {
            route = "/" + route;
        }
        return normalizeUrl(frontendPublicUrl) + route;
    }

}
