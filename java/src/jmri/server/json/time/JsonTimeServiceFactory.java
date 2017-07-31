package jmri.server.json.time;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonTimeServiceFactory implements JsonServiceFactory {

    public final static String TIME = "time";

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
