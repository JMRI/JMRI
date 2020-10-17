package jmri.server.json.oblock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jmri.*;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.JsonRequest;
import jmri.server.json.idtag.JsonIdTagHttpService;
import jmri.server.json.reporter.JsonReporter;
import jmri.server.json.reporter.JsonReporterHttpService;
import jmri.server.json.roster.JsonRosterHttpService;
import jmri.server.json.sensor.JsonSensor;

import javax.servlet.http.HttpServletResponse;

import static jmri.server.json.JSON.VALUE;
import static jmri.server.json.oblock.JsonOblock.OBLOCK;
import static jmri.server.json.oblock.JsonOblock.OBLOCKS;
import static jmri.server.json.idtag.JsonIdTag.IDTAG;
import static jmri.server.json.reporter.JsonReporter.REPORTER;

/**
 * @author mstevetodd Copyright 2018
 * @author Randall Wood Copyright 2018, 2019
 */
public class JsonOblockHttpService extends JsonNamedBeanHttpService<OBlock> {

    private JsonIdTagHttpService idTagService = new JsonIdTagHttpService(mapper);
    private JsonReporterHttpService reporterService = new JsonReporterHttpService(mapper);
    private JsonRosterHttpService rosterService = new JsonRosterHttpService(mapper);

    public JsonOblockHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(OBlock oblock, String name, String type, JsonRequest request) throws JsonException {
        ObjectNode root = this.getNamedBean(oblock, name, type, request);
        ObjectNode data = root.with(JSON.DATA);
        switch (oblock.getState()) {
            case Block.UNDETECTED:
            case NamedBean.UNKNOWN:
                data.put(JSON.STATE, JSON.UNKNOWN);
                break;
            default:
                data.put(JSON.STATE, oblock.getState());
        }
        // set block value based on type stored there
        Object bv = oblock.getValue();
        if (bv == null) {
            data.putNull(VALUE);
        } else if (bv instanceof jmri.IdTag) {
            ObjectNode idTagValue = idTagService.doGet((jmri.IdTag) bv, name, IDTAG, request);
            data.set(VALUE, idTagValue);
        } else if (bv instanceof Reporter) {
            ObjectNode reporterValue = reporterService.doGet((Reporter) bv, name, REPORTER, request);
            data.set(VALUE, reporterValue);
        } else if (bv instanceof BasicRosterEntry) {
            ObjectNode rosterValue = (ObjectNode) rosterService.getRosterEntry(request.locale, ((BasicRosterEntry) bv).getId(), request.id);
            data.set(VALUE, rosterValue);
        } else {
            // send string for types not explicitly handled
            data.put(VALUE, bv.toString());
        }
        data.put(JsonSensor.SENSOR, oblock.getSensor() != null ? oblock.getSensor().getSystemName() : null);
        data.put(JsonReporter.REPORTER, oblock.getReporter() != null ? oblock.getReporter().getSystemName() : null);
        data.put(JSON.SPEED, oblock.getBlockSpeed());
        data.put(JsonOblock.CURVATURE, oblock.getCurvature());
        data.put(JSON.DIRECTION, oblock.getDirection());
        data.put(JSON.LENGTH, oblock.getLengthMm());
        data.put(JsonOblock.PERMISSIVE, oblock.getPermissiveWorking());
        data.put(JsonOblock.SPEED_LIMIT, oblock.getSpeedLimit());
        ArrayNode array = data.putArray(JsonOblock.DENIED);
        oblock.getDeniedBlocks().forEach(array::add);
        return root;
    }

    @Override
    public ObjectNode doPost(OBlock oblock, String name, String type, JsonNode data, JsonRequest request)
            throws JsonException {
        if (!data.path(JSON.VALUE).isMissingNode()) {
            if (data.path(JSON.VALUE).isNull()) {
                oblock.setValue(null);
            } else {
                oblock.setValue(data.path(JSON.VALUE).asText());
            }
        }
        int state = data.path(JSON.STATE).asInt(JSON.UNKNOWN);
        switch (state) {
            case JSON.ACTIVE:
                oblock.setState(OBlock.OCCUPIED);
                break;
            case JSON.INACTIVE:
                oblock.setState(OBlock.UNOCCUPIED);
                break;
            case JSON.UNKNOWN:
                // leave state alone in this case
                break;
            default:
                throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorUnknownState", OBLOCK, state),
                        request.id);
        }
        if (!data.path(JsonSensor.SENSOR).isMissingNode()) {
            JsonNode node = data.path(JsonSensor.SENSOR);
            if (node.isNull()) {
                oblock.setSensor(null);
            } else {
                Sensor sensor = InstanceManager.getDefault(SensorManager.class).getBySystemName(node.asText());
                if (sensor != null) {
                    oblock.setSensor(sensor.getSystemName());
                } else {
                    throw new JsonException(404,
                            Bundle.getMessage(request.locale, JsonException.ERROR_NOT_FOUND, JsonSensor.SENSOR,
                                    node.asText()),
                            request.id);
                }
            }
        }
        if (!data.path(JsonReporter.REPORTER).isMissingNode()) {
            JsonNode node = data.path(JsonReporter.REPORTER);
            if (node.isNull()) {
                oblock.setReporter(null);
            } else {
                Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getBySystemName(node.asText());
                if (reporter != null) {
                    oblock.setReporter(reporter);
                } else {
                    throw new JsonException(404,
                            Bundle.getMessage(request.locale, JsonException.ERROR_NOT_FOUND, JsonReporter.REPORTER,
                                    node.asText()),
                            request.id);
                }
            }
        }
        String text = data.findPath(JSON.SPEED).asText(oblock.getBlockSpeed());
        try {
            oblock.setBlockSpeed(text);
        } catch (JmriException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                    Bundle.getMessage(request.locale, JsonException.ERROR_BAD_PROPERTY_VALUE, text, JSON.SPEED, type),
                    request.id);
        }
        oblock.setCurvature(data.path(JsonOblock.CURVATURE).asInt(oblock.getCurvature()));
        oblock.setDirection(data.path(JSON.DIRECTION).asInt(oblock.getDirection()));
        if (data.path(JSON.LENGTH).isNumber()) {
            oblock.setLength(data.path(JSON.LENGTH).floatValue());
        }
        oblock.setPermissiveWorking(data.path(JsonOblock.PERMISSIVE).asBoolean(oblock.getPermissiveWorking()));
        return this.doGet((OBlock) oblock, name, type, request);
    }

    @Override
    protected void doDelete(OBlock bean, String name, String type, JsonNode data, JsonRequest request)
            throws JsonException {
        deleteBean(bean, name, type, data, request);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case OBLOCK:
            case OBLOCKS:
                return doSchema(type,
                        server,
                        "jmri/server/json/oblock/oblock-server.json",
                        "jmri/server/json/oblock/oblock-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

    @Override
    protected String getType() {
        return OBLOCK;
    }

    @Override
    protected ProvidingManager<OBlock> getManager() {
        return InstanceManager.getDefault(OBlockManager.class);
    }
}
