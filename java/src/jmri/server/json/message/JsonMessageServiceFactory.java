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
    public String[] getTypes() {
        return new String[]{JSON.HELLO, JsonMessage.CLIENT};
    }

    @Override
    public String[] getSentTypes() {
        return new String[]{JsonMessage.MESSAGE};
    }

    @Override
    public JsonMessageSocketService getSocketService(JsonConnection connection) {
        return new JsonMessageSocketService(connection);
    }

    @Override
    public JsonMessageHttpService getHttpService(ObjectMapper mapper) {
        return new JsonMessageHttpService(mapper);
    }

}
