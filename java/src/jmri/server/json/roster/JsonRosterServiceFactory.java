package jmri.server.json.roster;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
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
    public JsonRosterSocketService getSocketService(JsonConnection connection) {
        return new JsonRosterSocketService(connection);
    }

    @Override
    public JsonRosterHttpService getHttpService(ObjectMapper mapper) {
        return new JsonRosterHttpService(mapper);
    }

}
