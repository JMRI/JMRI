package jmri.server.json.time;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;

/**
 *
 * @author Randall Wood
 */
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
