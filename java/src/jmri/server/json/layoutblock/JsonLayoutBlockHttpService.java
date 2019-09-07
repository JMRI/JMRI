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

import java.awt.Color;
import java.util.List;
import java.util.Locale;

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
    public JsonNode doGet(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        return doGet(InstanceManager.getDefault(LayoutBlockManager.class).getBeanBySystemName(name), name, type, locale, id);
    }

    @Override
    protected ObjectNode doGet(LayoutBlock layoutBlock, String name, String type, Locale locale, int id) throws JsonException {
        ObjectNode root = super.getNamedBean(layoutBlock, name, type, locale, id); // throws JsonException if layoutBlock == null
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
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        return doPost(InstanceManager.getDefault(LayoutBlockManager.class).getBeanBySystemName(name), data, name, type, locale, id);
    }

    public JsonNode doPost(LayoutBlock layoutBlock, JsonNode data, String name, String type, Locale locale, int id) throws JsonException {
        postNamedBean(layoutBlock, data, name, type, locale, id); // throws JsonException if layoutBlock == null
        Color color;
        String string = "";
        try {
            string = data.path(TRACK_COLOR).asText();
            color = (!data.path(TRACK_COLOR).isMissingNode() ? ColorUtil.stringToColor(string) : null);
            if (color != null) {
                layoutBlock.setBlockTrackColor(color);
            }
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, JsonException.ERROR_BAD_PROPERTY_VALUE, string, TRACK_COLOR, type), id);
        }
        try {
            string = data.path(OCCUPIED_COLOR).asText();
            color = (!data.path(OCCUPIED_COLOR).isMissingNode() ? ColorUtil.stringToColor(string) : null);
            if (color != null) {
                layoutBlock.setBlockTrackColor(color);
            }
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, JsonException.ERROR_BAD_PROPERTY_VALUE, string, OCCUPIED_COLOR, type), id);
        }
        try {
            string = data.path(EXTRA_COLOR).asText();
            color = (!data.path(EXTRA_COLOR).isMissingNode() ? ColorUtil.stringToColor(string) : null);
            if (color != null) {
                layoutBlock.setBlockTrackColor(color);
            }
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, JsonException.ERROR_BAD_PROPERTY_VALUE, string, EXTRA_COLOR, type), id);
        }
        if (!data.path(MEMORY).isMissingNode()) {
            string = !data.path(MEMORY).isNull() ? data.path(MEMORY).asText() : null;
            if (string != null) {
                Memory memory = InstanceManager.getDefault(MemoryManager.class).getBeanBySystemName(string);
                if (memory == null) {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, MEMORY, string), id);
                }
                layoutBlock.setMemoryName(memory.getUserName());
            } else {
                layoutBlock.setMemoryName(null);
            }
        }
        if (!data.path(OCCUPANCY_SENSOR).isMissingNode()) {
            string = !data.path(OCCUPANCY_SENSOR).isNull() ? data.path(OCCUPANCY_SENSOR).asText() : null;
            if (string != null) {
                Sensor sensor = InstanceManager.getDefault(SensorManager.class).getBeanBySystemName(string);
                if (sensor == null) {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, SENSOR, string), id);
                }
                layoutBlock.setOccupancySensorName(sensor.getUserName());
            } else {
                layoutBlock.setOccupancySensorName(null);
            }
        }
        layoutBlock.setUseExtraColor(data.path(USE_EXTRA_COLOR).asBoolean(layoutBlock.getUseExtraColor()));
        return doGet(layoutBlock, name, type, locale, id);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
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
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "ErrorEmptyAttribute", USERNAME, type), id);
            }
        }
        if (layoutBlock == null) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorInternal", type), id);
        }
        layoutBlock.initializeLayoutBlock();
        layoutBlock.initializeLayoutBlockRouting();
        return doPost(layoutBlock, data, name, type, locale, id);
    }

    @Override
    public void doDelete(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        LayoutBlock layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).getBeanBySystemName(name);
        if (layoutBlock == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, type, name), id);
        }
        List<String> listeners = layoutBlock.getListenerRefs();
        if (!listeners.isEmpty() && !acceptForceDeleteToken(type, name, data.path(JSON.FORCE_DELETE).asText())) {
            ArrayNode conflicts = mapper.createArrayNode();
            listeners.forEach(conflicts::add);
            throwDeleteConflictException(type, name, conflicts, locale, id);
        } else {
            InstanceManager.getDefault(LayoutBlockManager.class).deregister(layoutBlock);
        }
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, Locale locale, int id) throws JsonException {
        return doGetList(InstanceManager.getDefault(LayoutBlockManager.class), type, data, locale, id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case LAYOUTBLOCK:
            case LAYOUTBLOCKS:
                return doSchema(type,
                        server,
                        "jmri/server/json/layoutblock/layoutBlock-server.json",
                        "jmri/server/json/layoutblock/layoutBlock-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }
}
