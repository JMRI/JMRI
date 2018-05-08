package jmri.server.json.memory;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.VALUE;
import static jmri.server.json.memory.JsonMemory.MEMORIES;
import static jmri.server.json.memory.JsonMemory.MEMORY;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;

/**
 *
 * @author Randall Wood
 */
public class JsonMemoryHttpService extends JsonNamedBeanHttpService {

    public JsonMemoryHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        Memory memory = InstanceManager.memoryManagerInstance().getMemory(name);
        ObjectNode data = this.getNamedBean(memory, name, type, locale);
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, MEMORY);
        root.set(DATA, data);
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
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        Memory memory = InstanceManager.memoryManagerInstance().getMemory(name);
        if (memory == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", MEMORY, name));
        }
        this.postNamedBean(memory, data, name, type, locale);
        if (!data.path(VALUE).isMissingNode()) {
            if (data.path(VALUE).isNull()) {
                memory.setValue(null);
            } else {
                memory.setValue(data.path(VALUE).asText());
            }
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
        try {
            InstanceManager.memoryManagerInstance().provideMemory(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", MEMORY, name));
        }
        return this.doPost(type, name, data, locale);
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.memoryManagerInstance().getSystemNameList()) {
            root.add(this.doGet(MEMORY, name, locale));
        }
        return root;

    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale) throws JsonException {
        switch (type) {
            case MEMORY:
            case MEMORIES:
                return doSchema(type,
                        server,
                        "jmri/server/json/memory/memory-server.json",
                        "jmri/server/json/memory/memory-client.json");
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }
}
