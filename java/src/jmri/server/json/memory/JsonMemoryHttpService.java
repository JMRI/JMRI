package jmri.server.json.memory;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.VALUE;
import static jmri.server.json.memory.JsonMemory.MEMORIES;
import static jmri.server.json.memory.JsonMemory.MEMORY;
import static jmri.server.json.idtag.JsonIdTag.IDTAG;
import static jmri.server.json.reporter.JsonReporter.REPORTER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.servlet.http.HttpServletResponse;

import jmri.BasicRosterEntry;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.ProvidingManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.JsonRequest;
import jmri.server.json.idtag.JsonIdTagHttpService;
import jmri.server.json.reporter.JsonReporterHttpService;
import jmri.server.json.roster.JsonRosterHttpService;

/**
 * @author Randall Wood
 */
public class JsonMemoryHttpService extends JsonNamedBeanHttpService<Memory> {

    private JsonIdTagHttpService idTagService = new JsonIdTagHttpService(mapper);
    private JsonReporterHttpService reporterService = new JsonReporterHttpService(mapper);
    private JsonRosterHttpService rosterService = new JsonRosterHttpService(mapper);   

    public JsonMemoryHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(Memory memory, String name, String type, JsonRequest request) throws JsonException {
        ObjectNode root = this.getNamedBean(memory, name, getType(), request);
        ObjectNode data = root.with(DATA);
        if (memory != null) {
            Object val = memory.getValue();
            if (val == null) {
                data.putNull(VALUE);
            } else {
                // set memory value based on type
                if (val instanceof jmri.IdTag) {
                    ObjectNode idTagValue = idTagService.doGet((jmri.IdTag) val, name, IDTAG, request);
                    data.set(VALUE, idTagValue);
                } else if (val instanceof jmri.Reporter) {
                    ObjectNode reporterValue = reporterService.doGet((jmri.Reporter) val, name, REPORTER, request);
                    data.set(VALUE, reporterValue);
                } else if (val instanceof jmri.BasicRosterEntry) {
                    ObjectNode rosterValue = (ObjectNode) rosterService.getRosterEntry(request.locale, ((BasicRosterEntry) val).getId(), request.id);
                    data.set(VALUE, rosterValue);
                } else {
                    // send string for types not explicitly handled
                    data.put(VALUE, val.toString());
                }
            }
        }
        return root;
    }

    @Override
    public ObjectNode doPost(Memory memory, String name, String type, JsonNode data, JsonRequest request)
            throws JsonException {
        if (!data.path(VALUE).isMissingNode()) {
            if (data.path(VALUE).isNull()) {
                memory.setValue(null);
            } else {
                memory.setValue(data.path(VALUE).asText());
            }
        }
        return this.doGet(memory, name, type, request);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case MEMORY:
            case MEMORIES:
                return doSchema(type,
                        server,
                        "jmri/server/json/memory/memory-server.json",
                        "jmri/server/json/memory/memory-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
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
