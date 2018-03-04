package jmri.server.json.light;

/**
 * JSON Tokens used by the JMRI JSON Light service.
 *
 * @author Randall Wood (C) 2016
 */
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
