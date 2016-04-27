package jmri.server.json.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonMemoryServiceFactory implements JsonServiceFactory {

    public static final String MEMORY = "memory"; // NOI18N
    public static final String MEMORIES = "memories"; // NOI18N

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
