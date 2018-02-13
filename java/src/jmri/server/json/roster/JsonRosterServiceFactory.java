package jmri.server.json.roster;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonRosterServiceFactory implements JsonServiceFactory<JsonRosterHttpService, JsonRosterSocketService> {

    @Override
    public String[] getTypes() {
        return new String[]{JsonRoster.ROSTER, JsonRoster.ROSTER_ENTRY, JsonRoster.ROSTER_GROUPS, JsonRoster.ROSTER_GROUP};
    }

    @Override
    public JsonRosterSocketService getSocketService(JsonConnection connection) {
        return new JsonRosterSocketService(connection);
    }

    @Override
    public JsonRosterHttpService getHttpService(ObjectMapper mapper) {
        return new JsonRosterHttpService(mapper);
    }

}
