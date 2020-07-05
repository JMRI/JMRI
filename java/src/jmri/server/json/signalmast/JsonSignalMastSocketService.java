package jmri.server.json.signalmast;

import jmri.SignalMast;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood (C) 2016
 */
@API(status = EXPERIMENTAL)
public class JsonSignalMastSocketService extends JsonNamedBeanSocketService<SignalMast, JsonSignalMastHttpService> {

    public JsonSignalMastSocketService(JsonConnection connection) {
        super(connection, new JsonSignalMastHttpService(connection.getObjectMapper()));
    }
}
