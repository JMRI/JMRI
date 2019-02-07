package jmri.server.json.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for JSON service providers for handling {@link jmri.Route}s.
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonSchemaServiceFactory implements JsonServiceFactory<JsonSchemaHttpService, JsonSchemaSocketService> {

    @Override
    public String[] getTypes() {
        return new String[]{JSON.JSON, JSON.SCHEMA, JSON.TYPE};
    }

    @Override
    public JsonSchemaSocketService getSocketService(JsonConnection connection) {
        return new JsonSchemaSocketService(connection);
    }

    @Override
    public JsonSchemaHttpService getHttpService(ObjectMapper mapper) {
        return new JsonSchemaHttpService(mapper);
    }

}
