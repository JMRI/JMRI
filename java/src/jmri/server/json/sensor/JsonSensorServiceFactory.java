package jmri.server.json.sensor;

import static jmri.server.json.sensor.JsonSensor.SENSOR;
import static jmri.server.json.sensor.JsonSensor.SENSORS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for JSON services for {@link jmri.Sensor}s.
 * 
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonSensorServiceFactory implements JsonServiceFactory {


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
