package jmri.server.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * JsonServiceFactory for a JsonService supporting testing of the
 * JsonClientHandler.
 *
 * @author Randall Wood Copyright 2018
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonTestServiceFactory implements JsonServiceFactory<JsonTestHttpService, JsonTestSocketService> {

    public static final String TEST = "test";

    @Override
    public String[] getTypes(String version) {
        return new String[]{TEST};
    }

    @Override
    public JsonTestSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonTestSocketService(connection);
    }

    @Override
    public JsonTestHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonTestHttpService(mapper);
    }

}
