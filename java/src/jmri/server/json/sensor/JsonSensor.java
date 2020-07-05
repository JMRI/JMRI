package jmri.server.json.sensor;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Tokens used by the JMRI JSON Sensor service.
 *
 * @author Randall Wood (C) 2016
 */
@API(status = EXPERIMENTAL)
public class JsonSensor {

    public static final String SENSOR = "sensor"; // NOI18N
    public static final String SENSORS = "sensors"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonSensor() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}
