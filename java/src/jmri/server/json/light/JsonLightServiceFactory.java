package jmri.server.json.light;

import static jmri.server.json.light.JsonLight.LIGHT;
import static jmri.server.json.light.JsonLight.LIGHTS;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonLightServiceFactory implements JsonServiceFactory<JsonLightHttpService, JsonLightSocketService> {


    @Override
    public String[] getTypes() {
        return new String[]{LIGHT, LIGHTS};
    }

    @Override
    public JsonLightSocketService getSocketService(JsonConnection connection) {
        return new JsonLightSocketService(connection);
    }

    @Override
    public JsonLightHttpService getHttpService(ObjectMapper mapper) {
        return new JsonLightHttpService(mapper);
    }

}
