package cl.dsoto.webservice.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentityEventFeedPageResource {

    private List<IdentityEventFeedItemResource> items;
    private Long nextCursor;
    private boolean hasMore;
}
