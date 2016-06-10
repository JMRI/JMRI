package jmri.server.json.sensor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;

/**
 * Factory for JSON services for {@link jmri.Sensor}s.
 * 
 * @author Randall Wood
 */
public class JsonSensorServiceFactory implements JsonServiceFactory {

    public static final String SENSOR = "sensor"; // NOI18N
    public static final String SENSORS = "sensors"; // NOI18N

    @Override
    public String[] getTypes() {
        return new String[]{SENSOR, SENSORS};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonSensorSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return new JsonSensorHttpService(mapper);
    }

}
