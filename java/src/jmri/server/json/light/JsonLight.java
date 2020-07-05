package jmri.server.json.light;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * JSON Tokens used by the JMRI JSON Light service.
 *
 * @author Randall Wood (C) 2016
 */
@API(status = EXPERIMENTAL)
public class JsonLight {

    public static final String LIGHT = "light"; // NOI18N
    public static final String LIGHTS = "lights"; // NOI18N

    /**
     * Prevent instantiation, since this class only contains static values
     */
    private JsonLight() {
        throw new UnsupportedOperationException("There is no valid instance of this class");
    }
}
