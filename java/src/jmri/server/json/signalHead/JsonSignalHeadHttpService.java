package jmri.server.json.signalHead;

import static jmri.server.json.JSON.APPEARANCE;
import static jmri.server.json.JSON.APPEARANCE_NAME;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.INCONSISTENT;
import static jmri.server.json.JSON.LIT;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TOKEN_HELD;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.JSON.USERNAME;
import static jmri.server.json.signalHead.JsonSignalHead.SIGNAL_HEAD;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonSignalHeadHttpService extends JsonHttpService {

    public JsonSignalHeadHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, SIGNAL_HEAD);
        ObjectNode data = root.putObject(DATA);
        SignalHead signalHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);
        if (signalHead == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SIGNAL_HEAD, name));
        }
        data.put(NAME, name);
        data.put(USERNAME, signalHead.getUserName());
        data.put(COMMENT, signalHead.getComment());
        data.put(LIT, signalHead.getLit());
        data.put(APPEARANCE, signalHead.getAppearance());
        data.put(TOKEN_HELD, signalHead.getHeld());
        //state is appearance, plus a flag for held status
        if (signalHead.getHeld()) {
            data.put(STATE, SignalHead.HELD);
        } else {
            data.put(STATE, signalHead.getAppearance());
        }
        data.put(APPEARANCE_NAME, signalHead.getAppearanceName());
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        SignalHead signalHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);
        if (signalHead == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SIGNAL_HEAD, name));
        }
        if (data.path(USERNAME).isTextual()) {
            signalHead.setUserName(data.path(USERNAME).asText());
        }
        if (data.path(COMMENT).isTextual()) {
            signalHead.setComment(data.path(COMMENT).asText());
        }
        int state = data.path(STATE).asInt(UNKNOWN);
        boolean isValid = false;
        for (int validState : signalHead.getValidStates()) {
            if (state == validState) {
                isValid = true;
                break;
            }
        }
        if (isValid && state != INCONSISTENT && state != UNKNOWN) {
            // TODO: completely insulate JSON state from SignalHead state
            signalHead.setAppearance(state);
        } else {
            throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", SIGNAL_HEAD, state));
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.getDefault(SignalHeadManager.class).getSystemNameList()) {
            root.add(this.doGet(SIGNAL_HEAD, name, locale));
        }
        return root;
    }
}
