package jmri.server.json.memory;

import static jmri.server.json.memory.JsonMemory.MEMORIES;
import static jmri.server.json.memory.JsonMemory.MEMORY;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = JsonServiceFactory.class)
public class JsonMemoryServiceFactory implements JsonServiceFactory {


    @Override
    public String[] getTypes() {
        return new String[]{MEMORY, MEMORIES};
    }

    @Override
    public JsonSocketService getSocketService(JsonConnection connection) {
        return new JsonMemorySocketService(connection);
    }

    @Override
    public JsonHttpService getHttpService(ObjectMapper mapper) {
        return new JsonMemoryHttpService(mapper);
    }

}
