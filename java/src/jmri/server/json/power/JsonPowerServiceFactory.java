package jmri.server.json.power;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.jmris.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonPowerServiceFactory implements JsonServiceFactory {

    /**
     * Token for type and name for power status messages.
     * 
     * {@value #POWER}
     */
    public static final String POWER = "power";
    
    @Override
    public String[] getTypes() {
        String[] types = {POWER};
        return types;
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        // do not include in imports to prevent class loading if never called
        return new jmri.server.json.power.JsonPowerSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        // do not include in imports to prevent class loading if never called
        return new jmri.server.json.power.JsonPowerHttpService(mapper);
    }
    
}
