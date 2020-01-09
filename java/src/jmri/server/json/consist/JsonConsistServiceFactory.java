package jmri.server.json.consist;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood Copyright (C) 2016
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonConsistServiceFactory implements JsonServiceFactory<JsonConsistHttpService, JsonConsistSocketService> {


    @Override
    public String[] getTypes(String version) {
        return new String[]{JsonConsist.CONSIST, JsonConsist.CONSISTS};
    }

    @Override
    public JsonConsistSocketService getSocketService(JsonConnection connection, String version) {
        return new JsonConsistSocketService(connection);
    }

    @Override
    public JsonConsistHttpService getHttpService(ObjectMapper mapper, String version) {
        return new JsonConsistHttpService(mapper);
    }

}
