package cl.dsoto.webservice;

import cl.dsoto.webservice.resources.IdentityEventFeedPageResource;

public interface IdentityEventFeedWebService {

    IdentityEventFeedPageResource getIdentityEvents(Long after, Integer limit);
}
