package jmri.server.json.memory;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.VALUE;
import static jmri.server.json.memory.JsonMemory.MEMORIES;
import static jmri.server.json.memory.JsonMemory.MEMORY;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.ProvidingManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;

/**
 *
 * @author Randall Wood
 */
public class JsonMemoryHttpService extends JsonNamedBeanHttpService<Memory> {

    public JsonMemoryHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(Memory memory, String name, String type, Locale locale, int id) throws JsonException {
        ObjectNode root = this.getNamedBean(memory, name, type, locale, id);
        ObjectNode data = root.with(DATA);
        if (memory != null) {
            if (memory.getValue() == null) {
                data.putNull(VALUE);
            } else {
                data.put(VALUE, memory.getValue().toString());
            }
        }
        return root;
    }

    @Override
    public ObjectNode doPost(Memory memory, String name, String type, JsonNode data, Locale locale, int id) throws JsonException {
        if (!data.path(VALUE).isMissingNode()) {
            if (data.path(VALUE).isNull()) {
                memory.setValue(null);
            } else {
                memory.setValue(data.path(VALUE).asText());
            }
        }
        return this.doGet(memory, name, type, locale, id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case MEMORY:
            case MEMORIES:
                return doSchema(type,
                        server,
                        "jmri/server/json/memory/memory-server.json",
                        "jmri/server/json/memory/memory-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }

    @Override
    protected String getType() {
        return MEMORY;
    }

    @Override
    protected ProvidingManager<Memory> getManager() {
        return InstanceManager.getDefault(MemoryManager.class);
    }
}
