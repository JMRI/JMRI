package jmri.server.json.time;

import com.fasterxml.jackson.databind.ObjectMapper;

import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonTimeServiceFactory implements JsonServiceFactory<JsonTimeHttpService, JsonTimeSocketService> {

    /**
     * @deprecated since 4.15.6; use {@link JSON#TIME} instead
     */
    @Deprecated
    public final static String TIME = JSON.TIME;

    @Override
    public String[] getTypes() {
        return new String[]{TIME};
    }

    @Override
    public JsonTimeSocketService getSocketService(JsonConnection connection) {
        return new JsonTimeSocketService(connection);
    }

    @Override
    public JsonTimeHttpService getHttpService(ObjectMapper mapper) {
        return new JsonTimeHttpService(mapper);
    }

}
