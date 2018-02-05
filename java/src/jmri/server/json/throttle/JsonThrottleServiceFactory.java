package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonThrottleServiceFactory implements JsonServiceFactory<JsonThrottleHttpService, JsonThrottleSocketService> {

    @Override
    public String[] getTypes() {
        String[] types = {JsonThrottle.THROTTLE};
        return types;
    }

    @Override
    public JsonThrottleSocketService getSocketService(JsonConnection connection) {
        return new JsonThrottleSocketService(connection);
    }

    @Override
    public JsonThrottleHttpService getHttpService(ObjectMapper mapper) {
        return new JsonThrottleHttpService(mapper);
    }

}
