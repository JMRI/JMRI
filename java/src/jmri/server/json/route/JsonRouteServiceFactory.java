package jmri.server.json.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;

/**
 * Factory for JSON service providers for handling {@link jmri.Route}s.
 *
 * @author Randall Wood
 */
public class JsonRouteServiceFactory implements JsonServiceFactory {

    public static final String ROUTE = "route"; // NOI18N
    public static final String ROUTES = "routes"; // NOI18N

    @Override
    public String[] getTypes() {
        return new String[]{ROUTE, ROUTES};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonRouteSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return new JsonRouteHttpService(mapper);
    }

}
