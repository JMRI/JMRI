package jmri.server.json.layoutblock;

import static jmri.server.json.JSON.BLOCK_COLOR;
import static jmri.server.json.JSON.BLOCK_EXTRA_COLOR;
import static jmri.server.json.JSON.BLOCK_OCCUPIED_COLOR;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.NAME;
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
        LayoutBlock layoutblock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(name);
        if (layoutblock == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LAYOUTBLOCK, name));
        }
        data.put(NAME, layoutblock.getSystemName());
        data.put(USERNAME, layoutblock.getUserName());
        data.put(COMMENT, layoutblock.getComment());
        data.put(STATE, layoutblock.getState());
        data.put(USE_EXTRA_COLOR, layoutblock.getUseExtraColor());
        data.put(BLOCK_COLOR, jmri.util.ColorUtil.colorToColorName(layoutblock.getBlockColor()));
        data.put(TRACK_COLOR, jmri.util.ColorUtil.colorToColorName(layoutblock.getBlockTrackColor()));
        data.put(BLOCK_OCCUPIED_COLOR, jmri.util.ColorUtil.colorToColorName(layoutblock.getBlockOccupiedColor()));
        data.put(BLOCK_EXTRA_COLOR, jmri.util.ColorUtil.colorToColorName(layoutblock.getBlockExtraColor()));
        
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        LayoutBlock layoutblock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(name);
        if (layoutblock == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LAYOUTBLOCK, name));
        }
        if (data.path(USERNAME).isTextual()) {
            layoutblock.setUserName(data.path(USERNAME).asText());
        }
        if (data.path(COMMENT).isTextual()) {
            layoutblock.setComment(data.path(COMMENT).asText());
        }
        //layoutBlock.state is a bogus construct, so don't expect valid results from this
        if (!data.path(STATE).isMissingNode()) {
            layoutblock.setState(data.path(STATE).asInt());
        }
        return this.doGet(type, name, locale);
    }

//    @Override
//    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
//        try {
//            InstanceManager.getDefault(LayoutBlockManager.class).provideLayoutBlock(name);
//            InstanceManager.blockManagerInstance().provideBlock(name);
//        } catch (Exception ex) {
//            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", LAYOUTBLOCK, name));
//        }
//        return this.doPost(type, name, data, locale);
//    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.getDefault(LayoutBlockManager.class).getSystemNameList()) {
            root.add(this.doGet(LAYOUTBLOCK, name, locale));
        }
        return root;

    }
}
