package jmri.server.json.idtag;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;

import static jmri.server.json.idtag.JsonIdTag.IDTAG;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood Copyright 2019
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonIdTagServiceFactory implements JsonServiceFactory<JsonIdTagHttpService, JsonIdTagSocketService> {


    @Override
    public String[] getTypes() {
        return new String[]{IDTAG};
    }

    @Override
    public JsonIdTagSocketService getSocketService(JsonConnection connection) {
        return new JsonIdTagSocketService(connection);
    }

    @Override
    public JsonIdTagHttpService getHttpService(ObjectMapper mapper) {
        return new JsonIdTagHttpService(mapper);
    }

}
