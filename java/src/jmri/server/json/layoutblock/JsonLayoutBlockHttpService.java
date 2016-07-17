package jmri.server.json.layoutblock;

import static jmri.server.json.JSON.BLOCK_COLOR;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.EXTRA_COLOR;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.OCCUPANCY_SENSOR;
import static jmri.server.json.JSON.OCCUPIED_COLOR;
import static jmri.server.json.JSON.OCCUPIED_SENSE;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TRACK_COLOR;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.USERNAME;
import static jmri.server.json.JSON.USE_EXTRA_COLOR;
import static jmri.server.json.layoutblock.JsonLayoutBlockServiceFactory.LAYOUTBLOCK;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
/**
 *
 * @author mstevetodd Copyright (C) 2016 (copied from JsonMemoryHttpService)
 * @author Randall Wood
 */
public class JsonLayoutBlockHttpService extends JsonHttpService {

    public JsonLayoutBlockHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LAYOUTBLOCK);
        ObjectNode data = root.putObject(DATA);
        LayoutBlock layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(name);
        if (layoutBlock == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LAYOUTBLOCK, name));
        }
        data.put(NAME, layoutBlock.getSystemName());
        data.put(USERNAME, layoutBlock.getUserName());
        data.put(COMMENT, layoutBlock.getComment());
        data.put(STATE, layoutBlock.getState());
        data.put(USE_EXTRA_COLOR, layoutBlock.getUseExtraColor());
        data.put(BLOCK_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockColor()));
        data.put(TRACK_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockTrackColor()));
        data.put(OCCUPIED_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockOccupiedColor()));
        data.put(EXTRA_COLOR, jmri.util.ColorUtil.colorToColorName(layoutBlock.getBlockExtraColor()));
        data.put(OCCUPANCY_SENSOR, layoutBlock.getOccupancySensorName());
        data.put(OCCUPIED_SENSE, layoutBlock.getOccupiedSense());
        
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        LayoutBlock layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(name);
        if (layoutBlock == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LAYOUTBLOCK, name));
        }
        if (data.path(USERNAME).isTextual()) {
            layoutBlock.setUserName(data.path(USERNAME).asText());
        }
        if (data.path(COMMENT).isTextual()) {
            layoutBlock.setComment(data.path(COMMENT).asText());
        }
        //layoutBlock.state is a bogus construct, so don't expect valid results from this
        if (!data.path(STATE).isMissingNode()) {
            layoutBlock.setState(data.path(STATE).asInt());
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.getDefault(LayoutBlockManager.class).getSystemNameList()) {
            root.add(this.doGet(LAYOUTBLOCK, name, locale));
        }
        return root;

    }
}
