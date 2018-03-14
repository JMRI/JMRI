package jmri.server.json.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for JSON service providers for handling {@link jmri.Route}s.
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonRouteServiceFactory implements JsonServiceFactory<JsonRouteHttpService, JsonRouteSocketService> {

    public static final String ROUTE = "route"; // NOI18N
    public static final String ROUTES = "routes"; // NOI18N

    @Override
    public String[] getTypes() {
        return new String[]{ROUTE, ROUTES};
    }

    @Override
    public JsonRouteSocketService getSocketService(JsonConnection connection) {
        return new JsonRouteSocketService(connection);
    }

    @Override
    public JsonRouteHttpService getHttpService(ObjectMapper mapper) {
        return new JsonRouteHttpService(mapper);
    }

}
