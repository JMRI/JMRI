package jmri.server.json.light;

import jmri.Light;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood
 */
@API(status = EXPERIMENTAL)
public class JsonLightSocketService extends JsonNamedBeanSocketService<Light, JsonLightHttpService> {

    public JsonLightSocketService(JsonConnection connection) {
        super(connection, new JsonLightHttpService(connection.getObjectMapper()));
    }
}
