package jmri.server.json.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Service factory for the JSON messaging service.
 *
 * @author Randall Wood Copyright 2017
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonMessageServiceFactory implements JsonServiceFactory {

    @Override
    public String[] getTypes() {
        return new String[]{JSON.HELLO, JsonMessage.CLIENT};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonMessageSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return null;
    }

}
