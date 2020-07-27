package jmri.server.json.signalmast;

import jmri.SignalMast;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonSignalMastSocketService extends JsonNamedBeanSocketService<SignalMast, JsonSignalMastHttpService> {

    public JsonSignalMastSocketService(JsonConnection connection) {
        super(connection, new JsonSignalMastHttpService(connection.getObjectMapper()));
    }
}
