package jmri.server.json.signalMast;

import static jmri.server.json.JSON.ASPECT;
import static jmri.server.json.JSON.ASPECT_DARK;
import static jmri.server.json.JSON.ASPECT_HELD;
import static jmri.server.json.JSON.ASPECT_UNKNOWN;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.LIT;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TOKEN_HELD;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.USERNAME;
import static jmri.server.json.signalMast.JsonSignalMast.SIGNAL_MAST;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonSignalMastHttpService extends JsonHttpService {

    public JsonSignalMastHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, SIGNAL_MAST);
        ObjectNode data = root.putObject(DATA);
        SignalMast signalMast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(name);
        if (signalMast == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SIGNAL_MAST, name));
        }
        data.put(NAME, name);
        data.put(USERNAME, signalMast.getUserName());
        if (signalMast.getComment() != null) {
            data.put(COMMENT, signalMast.getComment());
        }
        String aspect = signalMast.getAspect();
        if (aspect == null) {
            aspect = ASPECT_UNKNOWN; //if null, set aspect to "Unknown"   
        }
        data.put(ASPECT, aspect);
        data.put(LIT, signalMast.getLit());
        data.put(TOKEN_HELD, signalMast.getHeld());
        //state is appearance, plus flags for held and dark statii
        if ((signalMast.getHeld()) && (signalMast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD) != null)) {
            data.put(STATE, ASPECT_HELD);
        } else if ((!signalMast.getLit()) && (signalMast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DARK) != null)) {
            data.put(STATE, ASPECT_DARK);
        } else {
            data.put(STATE, aspect);
        }
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        SignalMast signalMast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(name);
        if (signalMast == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SIGNAL_MAST, name));
        }
        if (data.path(USERNAME).isTextual()) {
            signalMast.setUserName(data.path(USERNAME).asText());
        }
        if (data.path(COMMENT).isTextual()) {
            signalMast.setComment(data.path(COMMENT).asText());
        }
        String aspect = data.path(ASPECT).asText();
        if (signalMast.getValidAspects().contains(aspect)) {
            signalMast.setAspect(aspect);
        } else {
            throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", SIGNAL_MAST, aspect));
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.getDefault(SignalMastManager.class).getSystemNameList()) {
            root.add(this.doGet(SIGNAL_MAST, name, locale));
        }
        return root;
    }
}
