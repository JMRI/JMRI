package jmri.server.json.oblock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jmri.*;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.JsonRequest;
import jmri.server.json.idtag.JsonIdTagHttpService;
import jmri.server.json.reporter.JsonReporterHttpService;
import jmri.server.json.roster.JsonRosterHttpService;
import jmri.server.json.sensor.JsonSensor;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jmri.server.json.JSON.VALUE;
import static jmri.server.json.idtag.JsonIdTag.IDTAG;
import static jmri.server.json.oblock.JsonOblock.OBLOCK;
import static jmri.server.json.oblock.JsonOblock.OBLOCKS;
import static jmri.server.json.reporter.JsonReporter.REPORTER;

/**
 * Copied from jmri/server/json/blocks.java
 *
 * @author mstevetodd Copyright 2018
 * @author Randall Wood Copyright 2018, 2019
 * @author Egbert Broerse Copyright 2020
 */
public class JsonOblockHttpService extends JsonNamedBeanHttpService<OBlock> {

//    private JsonIdTagHttpService idTagService = new JsonIdTagHttpService(mapper);
//    private JsonReporterHttpService reporterService = new JsonReporterHttpService(mapper);
//    private JsonRosterHttpService rosterService = new JsonRosterHttpService(mapper);

    public JsonOblockHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(OBlock oblock, String name, String type, JsonRequest request) throws JsonException {
        ObjectNode root = this.getNamedBean(oblock, name, getType(), request);
        ObjectNode data = root.with(JSON.DATA);
        log.debug("oblock.getState() = {}", oblock.getState());
        switch (oblock.getState()) {
            case OBlock.UNDETECTED:
            case NamedBean.UNKNOWN:
                data.put(JsonOblock.STATUS, JSON.UNKNOWN);
                break;
            default:
                // add OBlock status, includes special values for Allocated 0x10, OutOfService 0x40 etc.
                data.put(JsonOblock.STATUS, oblock.getState());
        }
        data.put(JsonSensor.SENSOR, oblock.getSensor() != null ? oblock.getSensor().getSystemName() : null);
        data.put(JsonOblock.WARRANT, oblock.getWarrant() != null ? oblock.getWarrant().getDisplayName() : null); // add OBlock Warrant name
        data.put(JsonOblock.TRAIN, oblock.getWarrant() != null ? oblock.getWarrant().getTrainName() : null); // add OBlock Warrant name

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
        int status = data.path(JsonOblock.STATUS).asInt(JSON.UNKNOWN);
        switch (status) {
            case JSON.ACTIVE:
                oblock.setState(OBlock.OCCUPIED);
                break;
            case JSON.INACTIVE:
                oblock.setState(OBlock.UNOCCUPIED);
                break;
            case JSON.UNKNOWN:
                // leave state alone in this case
                break;
            case JSON.ALLOCATED:
                oblock.setState(OBlock.ALLOCATED);
                break;
            case JSON.RUNNING:
                oblock.setState(OBlock.RUNNING);
                break;
            case JSON.OUT_OF_SERVICE:
                oblock.setState(OBlock.OUT_OF_SERVICE);
                break;
            case JSON.TRACK_ERROR:
                oblock.setState(OBlock.TRACK_ERROR);
                break;
            default:
                throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorUnknownState", OBLOCK, status),
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
        if (!data.path(JsonOblock.TRAIN).isMissingNode()) {
            String text = data.path(JsonOblock.TRAIN).asText(oblock.getBlockSpeed());
            oblock.getWarrant().setTrainName(text);
        }
        // TODO add Train, Warrant
        return this.doGet(oblock, name, type, request);
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

    private final static Logger log = LoggerFactory.getLogger(JsonOblockHttpService.class);

}
