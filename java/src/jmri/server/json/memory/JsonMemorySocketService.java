package jmri.server.json.memory;

import jmri.Memory;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

/**
 *
 * @author Randall Wood
 */
public class JsonMemorySocketService extends JsonNamedBeanSocketService<Memory, JsonMemoryHttpService> {

    public JsonMemorySocketService(JsonConnection connection) {
        super(connection,new JsonMemoryHttpService(connection.getObjectMapper()));
    }

}
