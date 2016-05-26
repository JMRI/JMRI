package jmri.server.json.memory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.Memory;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.USERNAME;
import static jmri.server.json.JSON.VALUE;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import static jmri.server.json.memory.JsonMemoryServiceFactory.MEMORY;

/**
 *
 * @author Randall Wood
 */
public class JsonMemoryHttpService extends JsonHttpService {

    public JsonMemoryHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, MEMORY);
        ObjectNode data = root.putObject(DATA);
        Memory memory = InstanceManager.memoryManagerInstance().getMemory(name);
        if (memory == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", MEMORY, name));
        }
        data.put(NAME, memory.getSystemName());
        data.put(USERNAME, memory.getUserName());
        data.put(COMMENT, memory.getComment());
        if (memory.getValue() == null) {
            data.putNull(VALUE);
        } else {
            data.put(VALUE, memory.getValue().toString());
        }
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        Memory memory = InstanceManager.memoryManagerInstance().getMemory(name);
        if (memory == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", MEMORY, name));
        }
        if (data.path(USERNAME).isTextual()) {
            memory.setUserName(data.path(USERNAME).asText());
        }
        if (data.path(COMMENT).isTextual()) {
            memory.setComment(data.path(COMMENT).asText());
        }
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
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.memoryManagerInstance().getSystemNameList()) {
            root.add(this.doGet(MEMORY, name, locale));
        }
        return root;

    }
}
