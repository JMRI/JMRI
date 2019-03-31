package jmri.server.json.light;

import jmri.Light;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

/**
 *
 * @author Randall Wood
 */
public class JsonLightSocketService extends JsonNamedBeanSocketService<Light, JsonLightHttpService> {

    public JsonLightSocketService(JsonConnection connection) {
        super(connection, new JsonLightHttpService(connection.getObjectMapper()));
    }
}
