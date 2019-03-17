package jmri.server.json.turnout;

import jmri.Turnout;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

/**
 *
 * @author Randall Wood
 */
public class JsonTurnoutSocketService extends JsonNamedBeanSocketService<Turnout, JsonTurnoutHttpService> {

    public JsonTurnoutSocketService(JsonConnection connection) {
        super(connection, new JsonTurnoutHttpService(connection.getObjectMapper()));
    }
}
