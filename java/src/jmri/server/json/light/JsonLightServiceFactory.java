package jmri.server.json.light;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonLightServiceFactory implements JsonServiceFactory {

    public static final String LIGHT = "light"; // NOI18N
    public static final String LIGHTS = "lights"; // NOI18N

    @Override
    public String[] getTypes() {
        return new String[]{LIGHT, LIGHTS};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonLightSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return new JsonLightHttpService(mapper);
    }

}
