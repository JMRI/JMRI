package jmri.server.json.sensor;

/**
 * Tokens used by the JMRI JSON Sensor service.
 *
 * @author Randall Wood (C) 2016
 */
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
