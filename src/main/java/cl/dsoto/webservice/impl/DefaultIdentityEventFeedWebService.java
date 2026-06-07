package cl.dsoto.webservice.impl;

import cl.dsoto.entities.IdentityEventLogEntryEntity;
import cl.dsoto.repositories.IdentityEventLogEntryRepository;
import cl.dsoto.webservice.IdentityEventFeedWebService;
import cl.dsoto.webservice.resources.IdentityEventFeedItemResource;
import cl.dsoto.webservice.resources.IdentityEventFeedPageResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Path("internal/identity-events")
@RolesAllowed({"ADMIN", "token.identity-events.read"})
public class DefaultIdentityEventFeedWebService implements IdentityEventFeedWebService {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 500;

    @Inject
    IdentityEventLogEntryRepository repository;

    @GET
    @Override
    public IdentityEventFeedPageResource getIdentityEvents(
            @QueryParam("after") Long after,
            @QueryParam("limit") Integer limit
    ) {
        int pageSize = normalizedLimit(limit);
        List<IdentityEventFeedItemResource> items = repository
                .findByIdGreaterThanOrderByIdAsc(
                        after == null ? 0L : after,
                        PageRequest.of(0, pageSize + 1)
                )
                .stream()
                .map(this::toResource)
                .toList();

        boolean hasMore = items.size() > pageSize;
        List<IdentityEventFeedItemResource> pageItems = hasMore
                ? items.subList(0, pageSize)
                : items;
        Long nextCursor = pageItems.isEmpty()
                ? after
                : pageItems.get(pageItems.size() - 1).getCursor();

        return new IdentityEventFeedPageResource(pageItems, nextCursor, hasMore);
    }

    private int normalizedLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private IdentityEventFeedItemResource toResource(IdentityEventLogEntryEntity entry) {
        return new IdentityEventFeedItemResource(
                entry.getId(),
                entry.getEventId(),
                entry.getEventType(),
                entry.getSubject(),
                entry.getRegistrationId(),
                entry.getOccurredAt()
        );
    }
}
