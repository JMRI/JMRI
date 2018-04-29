package jmri.server.json.power;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonPowerServiceFactory implements JsonServiceFactory<JsonPowerHttpService, JsonPowerSocketService> {

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
    public JsonPowerSocketService getSocketService(JsonConnection connection) {
        return new JsonPowerSocketService(connection);
    }

    @Override
    public JsonPowerHttpService getHttpService(ObjectMapper mapper) {
        return new JsonPowerHttpService(mapper);
    }

}
