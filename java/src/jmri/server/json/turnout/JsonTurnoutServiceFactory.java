package jmri.server.json.turnout;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonTurnoutServiceFactory implements JsonServiceFactory {

    public static final String TURNOUT = "turnout"; // NOI18N
    public static final String TURNOUTS = "turnouts"; // NOI18N

    @Override
    public String[] getTypes() {
        return new String[]{TURNOUT, TURNOUTS};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonTurnoutSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return new JsonTurnoutHttpService(mapper);
    }

}
