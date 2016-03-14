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

    /**
     * {@value #ROSTER}
     */
    public static final String ROSTER = "roster"; // NOI18N
    /**
     * {@value #ROSTER_ENTRY}
     */
    public static final String ROSTER_ENTRY = "rosterEntry"; // NOI18N
    /**
     * {@value #ROSTER_GROUP}
     */
    public static final String ROSTER_GROUP = "rosterGroup"; // NOI18N
    /**
     * {@value #ROSTER_GROUPS}
     */
    public static final String ROSTER_GROUPS = "rosterGroups"; // NOI18N

    @Override
    public String[] getTypes() {
        return new String[]{ROSTER, ROSTER_ENTRY, ROSTER_GROUPS, ROSTER_GROUP};
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
