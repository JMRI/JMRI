package jmri.server.json.layoutblock;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.USERNAME;
import static jmri.server.json.layoutblock.JsonLayoutBlock.BLOCK_COLOR;
import static jmri.server.json.layoutblock.JsonLayoutBlock.EXTRA_COLOR;
import static jmri.server.json.layoutblock.JsonLayoutBlock.LAYOUTBLOCK;
import static jmri.server.json.layoutblock.JsonLayoutBlock.LAYOUTBLOCKS;
import static jmri.server.json.layoutblock.JsonLayoutBlock.OCCUPANCY_SENSOR;
import static jmri.server.json.layoutblock.JsonLayoutBlock.OCCUPIED_COLOR;
import static jmri.server.json.layoutblock.JsonLayoutBlock.OCCUPIED_SENSE;
import static jmri.server.json.layoutblock.JsonLayoutBlock.TRACK_COLOR;
import static jmri.server.json.layoutblock.JsonLayoutBlock.USE_EXTRA_COLOR;
import static jmri.server.json.block.JsonBlock.BLOCK;
import static jmri.server.json.memory.JsonMemory.MEMORY;
import static jmri.server.json.sensor.JsonSensor.SENSOR;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.Color;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNonProvidedNamedBeanHttpService;
import jmri.server.json.JsonRequest;
import jmri.util.ColorUtil;

/**
 *
 * @author mstevetodd Copyright (C) 2018
 * @author Randall Wood
 */
public class JsonLayoutBlockHttpService extends JsonNonProvidedNamedBeanHttpService<LayoutBlock> {

    public JsonLayoutBlockHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "check here for null is complexity for a situation that should not be possible")
    public JsonNode doGet(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        return doGet(InstanceManager.getDefault(LayoutBlockManager.class).getBySystemName(name), name, type, request);
    }

    @Override
    protected ObjectNode doGet(LayoutBlock layoutBlock, String name, String type, JsonRequest request) throws JsonException {
        ObjectNode root = super.getNamedBean(layoutBlock, name, type, request); // throws JsonException if layoutBlock == null
        ObjectNode data = root.with(DATA);
        data.put(STATE, layoutBlock.getState());
        data.put(USE_EXTRA_COLOR, layoutBlock.getUseExtraColor());
        data.put(BLOCK_COLOR, ColorUtil.colorToColorName(layoutBlock.getBlockColor()));
        data.put(TRACK_COLOR, ColorUtil.colorToColorName(layoutBlock.getBlockTrackColor()));
        data.put(OCCUPIED_COLOR, ColorUtil.colorToColorName(layoutBlock.getBlockOccupiedColor()));
        data.put(EXTRA_COLOR, ColorUtil.colorToColorName(layoutBlock.getBlockExtraColor()));
        if (layoutBlock.getOccupancySensor() != null) {
            data.put(OCCUPANCY_SENSOR, layoutBlock.getOccupancySensor().getSystemName());
        } else {
            data.putNull(OCCUPANCY_SENSOR);
        }
        if (layoutBlock.getMemory() != null) {
            data.put(MEMORY, layoutBlock.getMemory().getSystemName());
        } else {
            data.putNull(MEMORY);
        }
        if (layoutBlock.getBlock() != null) {
            data.put(BLOCK, layoutBlock.getBlock().getSystemName());
        } else {
            data.putNull(BLOCK);
        }
        data.put(OCCUPIED_SENSE, layoutBlock.getOccupiedSense());
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        return doPost(InstanceManager.getDefault(LayoutBlockManager.class).getBySystemName(name), data, name, type, request);
    }

    public JsonNode doPost(LayoutBlock layoutBlock, JsonNode data, String name, String type, JsonRequest request) throws JsonException {
        postNamedBean(layoutBlock, data, name, type, request); // throws JsonException if layoutBlock == null
        setBlockTrackColor(layoutBlock, TRACK_COLOR, type, data, request);
        setBlockTrackColor(layoutBlock, OCCUPIED_COLOR, type, data, request);
        setBlockTrackColor(layoutBlock, EXTRA_COLOR, type, data, request);
        String string = "";
        if (!data.path(MEMORY).isMissingNode()) {
            string = !data.path(MEMORY).isNull() ? data.path(MEMORY).asText() : null;
            if (string != null) {
                Memory memory = InstanceManager.getDefault(MemoryManager.class).getBySystemName(string);
                if (memory == null) {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(request.locale, JsonException.ERROR_NOT_FOUND, MEMORY, string), request.id);
                }
                layoutBlock.setMemoryName(memory.getUserName());
            } else {
                layoutBlock.setMemoryName(null);
            }
        }
        if (!data.path(OCCUPANCY_SENSOR).isMissingNode()) {
            string = !data.path(OCCUPANCY_SENSOR).isNull() ? data.path(OCCUPANCY_SENSOR).asText() : null;
            if (string != null) {
                Sensor sensor = InstanceManager.getDefault(SensorManager.class).getBySystemName(string);
                if (sensor == null) {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(request.locale, JsonException.ERROR_NOT_FOUND, SENSOR, string), request.id);
                }
                layoutBlock.setOccupancySensorName(sensor.getUserName());
            } else {
                layoutBlock.setOccupancySensorName(null);
            }
        }
        layoutBlock.setUseExtraColor(data.path(USE_EXTRA_COLOR).asBoolean(layoutBlock.getUseExtraColor()));
        return doGet(layoutBlock, name, type, request);
    }

    private void setBlockTrackColor(LayoutBlock layoutBlock, String key, String type, JsonNode data, JsonRequest request) throws JsonException {
        String value = "";
        try {
            value = data.path(key).asText();
            Color color = (!data.path(key).isMissingNode() ? ColorUtil.stringToColor(value) : null);
            if (color != null) {
                layoutBlock.setBlockTrackColor(color);
            }
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(request.locale, JsonException.ERROR_BAD_PROPERTY_VALUE, value, key, type), request.id);
        }
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        LayoutBlock layoutBlock = null;
        String systemName = name;
        // an empty name should be null to create a LayoutBlock with a generated systemName
        // the name would come across as an empty string even if null in the JSON payload
        // because JsonNode.asText() returns an empty String when JsonNode.isNull() == true
        if (systemName.isEmpty()) {
            systemName = null;
        }
        if (data.path(USERNAME).isMissingNode() || data.path(USERNAME).isNull()) {
            layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock();
        } else {
            String userName = data.path(USERNAME).asText();
            if (!userName.isEmpty()) {
                layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(systemName, userName);
            } else {
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(request.locale, "ErrorEmptyAttribute", USERNAME, type), request.id);
            }
        }
        if (layoutBlock == null) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(request.locale, "ErrorInternal", type), request.id);
        }
        layoutBlock.initializeLayoutBlock();
        layoutBlock.initializeLayoutBlockRouting();
        return doPost(layoutBlock, data, name, type, request);
    }

    @Override
    public void doDelete(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        LayoutBlock layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).getBySystemName(name);
        if (layoutBlock == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(request.locale, JsonException.ERROR_NOT_FOUND, type, name), request.id);
        }
        List<String> listeners = layoutBlock.getListenerRefs();
        if (!listeners.isEmpty() && !acceptForceDeleteToken(type, name, data.path(JSON.FORCE_DELETE).asText())) {
            ArrayNode conflicts = mapper.createArrayNode();
            listeners.forEach(conflicts::add);
            throwDeleteConflictException(type, name, conflicts, request);
        } else {
            InstanceManager.getDefault(LayoutBlockManager.class).deregister(layoutBlock);
        }
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, JsonRequest request) throws JsonException {
        return doGetList(InstanceManager.getDefault(LayoutBlockManager.class), LAYOUTBLOCK, data, request);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case LAYOUTBLOCK:
            case LAYOUTBLOCKS:
                return doSchema(type,
                        server,
                        "jmri/server/json/layoutblock/layoutBlock-server.json",
                        "jmri/server/json/layoutblock/layoutBlock-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

    @Override
    public LayoutBlock getNamedBean(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        try {
            if (!data.isEmpty() && !data.isNull()) {
                if (JSON.PUT.equals(request.method)) {
                    doPut(type, name, data, request);
                } else if (JSON.POST.equals(request.method)) {
                    doPost(type, name, data, request);
                }
            }
            return InstanceManager.getDefault(LayoutBlockManager.class).getBySystemName(name);
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(request.locale, "ErrorInvalidSystemName", name, type), request.id);
        }
    }
}
