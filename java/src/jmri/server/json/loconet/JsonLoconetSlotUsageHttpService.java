package jmri.server.json.loconet;

import static jmri.server.json.loconet.JsonLoconetSlotUsage.LOCONET_SLOT_USAGE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;
import jmri.jmrix.loconet.SlotMapEntry.SlotType;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonRequest;

/**
 * Reports real LocoNet loco-slot table usage (used/free/total) for the
 * active connection's command station.
 *
 * Read-only -- iterates the SlotManager's already in-memory slot array,
 * sending no new LocoNet traffic of its own. "total" reflects the
 * connected command station's real per-model capacity (from the same
 * LnCommandStationType data JMRI already uses to auto-detect the
 * connection), not JMRI's generic upper bound, which is sized for the
 * largest command station JMRI knows about and would overstate real
 * capacity for anything smaller.
 *
 * @author Andrew Deak Copyright (C) 2026
 */
public class JsonLoconetSlotUsageHttpService extends JsonHttpService {

    public JsonLoconetSlotUsageHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        List<LocoNetSystemConnectionMemo> memos = InstanceManager.getList(LocoNetSystemConnectionMemo.class);
        if (memos.isEmpty()) {
            throw new JsonException(503, "No LocoNet connection is available", request.id);
        }
        SlotManager sm = memos.get(0).getSlotManager();
        if (sm == null) {
            throw new JsonException(503, "No LocoNet slot manager is available", request.id);
        }
        int total = 0;
        int used = 0;
        for (int i = 0; i < sm.getNumSlots(); i++) {
            LocoNetSlot s = sm.slot(i);
            if (s.getSlotType() == SlotType.LOCO) {
                total++;
                // An Idle slot (dispatched or released, address still
                // resident but not actively driven) is capacity the command
                // station will reclaim for the next engine added. Only
                // In-Use/Common hold a live, actively-driven address that
                // isn't available for a different loco.
                int status = s.slotStatus() & LnConstants.LOCOSTAT_MASK;
                if (status == LnConstants.LOCO_IN_USE || status == LnConstants.LOCO_COMMON) {
                    used++;
                }
            }
        }
        ObjectNode dataNode = mapper.createObjectNode();
        dataNode.put("commandStation",
                sm.getCommandStationType() != null ? sm.getCommandStationType().getName() : null);
        dataNode.put("used", used);
        dataNode.put("total", total);
        dataNode.put("free", total - used);
        return message(LOCONET_SLOT_USAGE, dataNode, request.id);
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        // read-only service -- POST behaves the same as GET
        return doGet(type, name, data, request);
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, JsonRequest request) throws JsonException {
        // single-object service, no list distinction -- same as GET
        return doGet(type, "", data, request);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        // An unrecognized type must throw, not silently return this
        // service's own schema regardless of what was asked for -- matches
        // the pattern used by every other JsonHttpService (e.g.
        // JsonIdTagHttpService).
        if (LOCONET_SLOT_USAGE.equals(type)) {
            return doSchema(type, server,
                    "jmri/server/json/loconet/loconetSlotUsage-server.json",
                    "jmri/server/json/loconet/loconetSlotUsage-client.json",
                    request.id);
        }
        throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
    }
}
