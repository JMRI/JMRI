package jmri.server.json.sensor;

import jmri.Sensor;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * JSON Socket service for {@link jmri.Sensor}s.
 *
 * @author Randall Wood
 */
@API(status = EXPERIMENTAL)
public class JsonSensorSocketService extends JsonNamedBeanSocketService<Sensor, JsonSensorHttpService> {

    public JsonSensorSocketService(JsonConnection connection) {
        super(connection, new JsonSensorHttpService(connection.getObjectMapper()));
    }
}
