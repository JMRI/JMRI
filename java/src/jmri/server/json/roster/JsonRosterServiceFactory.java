package jmri.server.json.roster;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonRosterServiceFactory implements JsonServiceFactory {

    @Override
    public String[] getTypes() {
        return new String[]{JsonRoster.ROSTER, JsonRoster.ROSTER_ENTRY, JsonRoster.ROSTER_GROUPS, JsonRoster.ROSTER_GROUP};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonRosterSocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return new JsonRosterHttpService(mapper);
    }

}
