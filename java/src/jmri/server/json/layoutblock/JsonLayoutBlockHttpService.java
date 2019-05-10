package jmri.server.json.layoutblock;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.layoutblock.JsonLayoutBlock.BLOCK_COLOR;
import static jmri.server.json.layoutblock.JsonLayoutBlock.EXTRA_COLOR;
import static jmri.server.json.layoutblock.JsonLayoutBlock.LAYOUTBLOCK;
import static jmri.server.json.layoutblock.JsonLayoutBlock.LAYOUTBLOCKS;
import static jmri.server.json.layoutblock.JsonLayoutBlock.OCCUPANCY_SENSOR;
import static jmri.server.json.layoutblock.JsonLayoutBlock.OCCUPIED_COLOR;
import static jmri.server.json.layoutblock.JsonLayoutBlock.OCCUPIED_SENSE;
import static jmri.server.json.layoutblock.JsonLayoutBlock.TRACK_COLOR;
import static jmri.server.json.layoutblock.JsonLayoutBlock.USE_EXTRA_COLOR;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNonProvidedNamedBeanHttpService;

/**
 *
 * @author mstevetodd Copyright (C) 2018 (copied from JsonMemoryHttpService)
 * @author Randall Wood
 */
public class JsonLayoutBlockHttpService extends JsonNonProvidedNamedBeanHttpService<LayoutBlock> {

    public JsonLayoutBlockHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, Locale locale) throws JsonException {
        return doGet(InstanceManager.getDefault(LayoutBlockManager.class).getBeanBySystemName(name), name, type, locale);
    }

    @Override
    protected ObjectNode doGet(LayoutBlock layoutBlock, String name, String type, Locale locale) throws JsonException {
        ObjectNode root = super.getNamedBean(layoutBlock, name, type, locale); // throws JsonException if layoutBlock == null
        ObjectNode data = root.with(DATA);
        if (layoutBlock != null) {
            data.put(STATE, layoutBlock.getState());
            data.put(USE_EXTRA_COLOR, layoutBlock.getUseExtraColor());
            data.put(BLOCK_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockColor()));
            data.put(TRACK_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockTrackColor()));
            data.put(OCCUPIED_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockOccupiedColor()));
            data.put(EXTRA_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockExtraColor()));
            if (layoutBlock.getOccupancySensor() != null) {
                data.put(OCCUPANCY_SENSOR, layoutBlock.getOccupancySensor().getSystemName());
            } else {
                data.putNull(OCCUPANCY_SENSOR);
            }
            data.put(OCCUPIED_SENSE, layoutBlock.getOccupiedSense());
        }
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        LayoutBlock layoutBlock = this.postNamedBean(InstanceManager.getDefault(LayoutBlockManager.class).getBeanBySystemName(name), data, name, type, locale);
        //layoutBlock.state is a bogus construct, so don't expect valid results from this
        if (!data.path(STATE).isMissingNode()) {
            layoutBlock.setState(data.path(STATE).asInt());
        }
        return this.doGet(type, name, data, locale);
    }

    @Override
    public ArrayNode doGetList(String type, JsonNode data, Locale locale) throws JsonException {
        return doGetList(InstanceManager.getDefault(LayoutBlockManager.class), type, data, locale);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale) throws JsonException {
        switch (type) {
            case LAYOUTBLOCK:
            case LAYOUTBLOCKS:
                return doSchema(type,
                        server,
                        "jmri/server/json/layoutblock/layoutBlock-server.json",
                        "jmri/server/json/layoutblock/layoutBlock-client.json");
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }
}
