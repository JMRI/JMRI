package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonThrottleServiceFactory implements JsonServiceFactory {
    
    @Override
    public String[] getTypes() {
        String[] types = {JsonThrottle.THROTTLE};
        return types;
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonThrottleSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return null;
    }
    
}
