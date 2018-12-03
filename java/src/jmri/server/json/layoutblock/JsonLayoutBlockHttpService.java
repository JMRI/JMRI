package jmri.server.json.layoutblock;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TYPE;
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
import jmri.server.json.JsonNamedBeanHttpService;
/**
 *
 * @author mstevetodd Copyright (C) 2018 (copied from JsonMemoryHttpService)
 * @author Randall Wood
 */
public class JsonLayoutBlockHttpService extends JsonNamedBeanHttpService {

    public JsonLayoutBlockHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LAYOUTBLOCK);
        LayoutBlock layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(name);
        if (layoutBlock == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LAYOUTBLOCK, name));
        }
        ObjectNode data = super.getNamedBean(layoutBlock, name, LAYOUTBLOCK, locale);
        root.set(DATA, data);
        data.put(STATE, layoutBlock.getState());
        data.put(USE_EXTRA_COLOR, layoutBlock.getUseExtraColor());
        data.put(BLOCK_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockColor()));
        data.put(TRACK_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockTrackColor()));
        data.put(OCCUPIED_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockOccupiedColor()));
        data.put(EXTRA_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockExtraColor()));
        data.put(OCCUPANCY_SENSOR, layoutBlock.getOccupancySensor() != null ? layoutBlock.getOccupancySensorName() : null);
        data.put(OCCUPIED_SENSE, layoutBlock.getOccupiedSense());

        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        LayoutBlock layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(name);
        if (layoutBlock == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LAYOUTBLOCK, name));
        }
        this.postNamedBean(layoutBlock, data, name, type, locale);
        //layoutBlock.state is a bogus construct, so don't expect valid results from this
        if (!data.path(STATE).isMissingNode()) {
            layoutBlock.setState(data.path(STATE).asInt());
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.getDefault(LayoutBlockManager.class).getSystemNameList()) {
            root.add(this.doGet(LAYOUTBLOCK, name, locale));
        }
        return root;

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
