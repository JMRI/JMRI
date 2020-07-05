package jmri.server.json.memory;

import jmri.Memory;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood
 */
@API(status = EXPERIMENTAL)
public class JsonMemorySocketService extends JsonNamedBeanSocketService<Memory, JsonMemoryHttpService> {

    public JsonMemorySocketService(JsonConnection connection) {
        super(connection,new JsonMemoryHttpService(connection.getObjectMapper()));
    }

}
