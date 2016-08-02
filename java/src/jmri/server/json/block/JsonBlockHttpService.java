package jmri.server.json.block;

import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.USERNAME;
import static jmri.server.json.JSON.VALUE;
import static jmri.server.json.block.JsonBlockServiceFactory.BLOCK;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author mstevetodd Copyright (C) 2016 (copied from JsonMemoryHttpService)
 * @author Randall Wood
 */
public class JsonBlockHttpService extends JsonHttpService {

    public JsonBlockHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, BLOCK);
        ObjectNode data = root.putObject(DATA);
        Block block = InstanceManager.getDefault(BlockManager.class).getBlock(name);
        if (block == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", BLOCK, name));
        }
        data.put(NAME, block.getSystemName());
        data.put(USERNAME, block.getUserName());
        data.put(COMMENT, block.getComment());
        if (block.getValue() == null) {
            data.putNull(VALUE);
        } else {
            data.put(VALUE, block.getValue().toString());
        }
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        Block block = InstanceManager.getDefault(BlockManager.class).getBlock(name);
        if (block == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", BLOCK, name));
        }
        if (data.path(USERNAME).isTextual()) {
            block.setUserName(data.path(USERNAME).asText());
        }
        if (data.path(COMMENT).isTextual()) {
            block.setComment(data.path(COMMENT).asText());
        }
        if (!data.path(VALUE).isMissingNode()) {
            if (data.path(VALUE).isNull()) {
                block.setValue(null);
            } else {
                block.setValue(data.path(VALUE).asText());
            }
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
        try {
        InstanceManager.getDefault(BlockManager.class).provideBlock(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", BLOCK, name));
        }
        return this.doPost(type, name, data, locale);
    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.getDefault(BlockManager.class).getSystemNameList()) {
            root.add(this.doGet(BLOCK, name, locale));
        }
        return root;

    }
}
