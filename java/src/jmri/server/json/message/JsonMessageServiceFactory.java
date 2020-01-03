package jmri.server.json.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Service factory for the JSON messaging service.
 *
 * @author Randall Wood Copyright 2017
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonMessageServiceFactory implements JsonServiceFactory<JsonMessageHttpService, JsonMessageSocketService> {

    @Override
    public String[] getTypes(String version) {
        return new String[]{JSON.HELLO, JsonMessage.CLIENT};
    }

    @Override
    public String[] getSentTypes(String version) {
        return new String[]{JsonMessage.MESSAGE};
    }

    @Override
    public JsonMessageSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonMessageSocketService(connection);
    }

    @Override
    public JsonMessageHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonMessageHttpService(mapper);
    }

}
