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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.ProvidingManager;
import jmri.Reportable;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;

/**
 *
 * @author Randall Wood
 */
public class JsonMemoryHttpService extends JsonNamedBeanHttpService<Memory> {

    private static final Logger log = LoggerFactory.getLogger(JsonMemoryHttpService.class);

    public JsonMemoryHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(Memory memory, String name, String type, Locale locale, int id) throws JsonException {
        ObjectNode root = this.getNamedBean(memory, name, type, locale, id);
        ObjectNode data = root.with(DATA);
        if (memory != null) {
            Object val = memory.getValue();
            if (val == null) {
                data.putNull(VALUE);
            } else {
                //convert memory to a string, logic copied from jmri.jmrit.display.layoutEditor.MemoryIcon
                String s = "";
                if (val instanceof String) {
                    s = (String) val;
                } else if (val instanceof javax.swing.ImageIcon) {
                    log.warn("ImageIcon not yet supported");
                } else if (val instanceof Number) {
                    s = val.toString();
                } else if (val instanceof jmri.IdTag){
                    // most IdTags are Reportable objects, so 
                    // this needs to be before Reportable
                    s = ((jmri.IdTag)val).getDisplayName();
                } else if (val instanceof Reportable) {
                    s = ((Reportable)val).toReportString();
                } else {
                    log.warn("can't return current value of memory '{}', val='{}' of Class '{}'", 
                            name, val, val.getClass().getName());
                }
                data.put(VALUE, s);
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
