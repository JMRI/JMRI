package jmri.server.json.turnout;

import jmri.Turnout;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood
 */
@API(status = EXPERIMENTAL)
public class JsonTurnoutSocketService extends JsonNamedBeanSocketService<Turnout, JsonTurnoutHttpService> {

    public JsonTurnoutSocketService(JsonConnection connection) {
        super(connection, new JsonTurnoutHttpService(connection.getObjectMapper()));
    }
}
