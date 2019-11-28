package jmri.server.json.sensor;

import jmri.Sensor;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

/**
 * JSON Socket service for {@link jmri.Sensor}s.
 *
 * @author Randall Wood
 */
public class JsonSensorSocketService extends JsonNamedBeanSocketService<Sensor, JsonSensorHttpService> {

    public JsonSensorSocketService(JsonConnection connection) {
        super(connection, new JsonSensorHttpService(connection.getObjectMapper()));
    }
}
