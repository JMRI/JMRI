package jmri.server.json.block;

import static jmri.server.json.block.JsonBlock.BLOCK;
import static jmri.server.json.block.JsonBlock.BLOCKS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.ProvidingManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.reporter.JsonReporter;
import jmri.server.json.sensor.JsonSensor;

/**
 *
 * @author mstevetodd Copyright 2018
 * @author Randall Wood Copyright 2018, 2019
 */
public class JsonBlockHttpService extends JsonNamedBeanHttpService<Block> {

    public JsonBlockHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(Block block, String name, String type, Locale locale, int id) throws JsonException {
        ObjectNode root = this.getNamedBean(block, name, type, locale, id); // throws JsonException if block == null
        ObjectNode data = root.with(JSON.DATA);
        switch (block.getState()) {
            case Block.UNDETECTED:
            case NamedBean.UNKNOWN:
                data.put(JSON.STATE, JSON.UNKNOWN);
                break;
            default:
                data.put(JSON.STATE, block.getState());
        }
        data.put(JSON.VALUE, block.getValue() != null ? block.getValue().toString() : null);
        data.put(JsonSensor.SENSOR, block.getSensor() != null ? block.getSensor().getSystemName() : null);
        data.put(JsonReporter.REPORTER, block.getReporter() != null ? block.getReporter().getSystemName() : null);
        data.put(JSON.SPEED, block.getBlockSpeed());
        data.put(JsonBlock.CURVATURE, block.getCurvature());
        data.put(JSON.DIRECTION, block.getDirection());
        data.put(JSON.LENGTH, block.getLengthMm());
        data.put(JsonBlock.PERMISSIVE, block.getPermissiveWorking());
        data.put(JsonBlock.SPEED_LIMIT, block.getSpeedLimit());
        ArrayNode array = data.putArray(JsonBlock.DENIED);
        block.getDeniedBlocks().forEach(array::add);
        return root;
    }

    @Override
    public ObjectNode doPost(Block block, String name, String type, JsonNode data, Locale locale, int id)
            throws JsonException {
        if (!data.path(JSON.VALUE).isMissingNode()) {
            if (data.path(JSON.VALUE).isNull()) {
                block.setValue(null);
            } else {
                block.setValue(data.path(JSON.VALUE).asText());
            }
        }
        int state = data.path(JSON.STATE).asInt(JSON.UNKNOWN);
        switch (state) {
            case JSON.ACTIVE:
                block.setState(Block.OCCUPIED);
                break;
            case JSON.INACTIVE:
                block.setState(Block.UNOCCUPIED);
                break;
            case JSON.UNKNOWN:
                // leave state alone in this case
                break;
            default:
                throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", BLOCK, state), id);
        }
        if (!data.path(JsonSensor.SENSOR).isMissingNode()) {
            JsonNode node = data.path(JsonSensor.SENSOR);
            if (node.isNull()) {
                block.setSensor(null);
            } else {
                Sensor sensor = InstanceManager.getDefault(SensorManager.class).getBySystemName(node.asText());
                if (sensor != null) {
                    block.setSensor(sensor.getSystemName());
                } else {
                    throw new JsonException(404,
                            Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, JsonSensor.SENSOR, node.asText()), id);
                }
            }
        }
        if (!data.path(JsonReporter.REPORTER).isMissingNode()) {
            JsonNode node = data.path(JsonReporter.REPORTER);
            if (node.isNull()) {
                block.setReporter(null);
            } else {
                Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getBySystemName(node.asText());
                if (reporter != null) {
                    block.setReporter(reporter);
                } else {
                    throw new JsonException(404,
                            Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, JsonReporter.REPORTER, node.asText()), id);
                }
            }
        }
        String text = data.findPath(JSON.SPEED).asText(block.getBlockSpeed());
        try {
            block.setBlockSpeed(text);
        } catch (JmriException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, JsonException.ERROR_BAD_PROPERTY_VALUE, text, JSON.SPEED, type), id);
        }
        block.setCurvature(data.path(JsonBlock.CURVATURE).asInt(block.getCurvature()));
        block.setDirection(data.path(JSON.DIRECTION).asInt(block.getDirection()));
        if (data.path(JSON.LENGTH).isNumber()) {
            block.setLength(data.path(JSON.LENGTH).floatValue());
        }
        block.setPermissiveWorking(data.path(JsonBlock.PERMISSIVE).asBoolean(block.getPermissiveWorking()));
        return this.doGet(block, name, type, locale, id);
    }

    @Override
    protected void doDelete(Block bean, String name, String type, JsonNode data, Locale locale, int id)
            throws JsonException {
        deleteBean(bean, name, type, data, locale, id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case BLOCK:
            case BLOCKS:
                return doSchema(type,
                        server,
                        "jmri/server/json/block/block-server.json",
                        "jmri/server/json/block/block-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }

    @Override
    protected String getType() {
        return BLOCK;
    }

    @Override
    protected ProvidingManager<Block> getManager() {
        return InstanceManager.getDefault(BlockManager.class);
    }
}
