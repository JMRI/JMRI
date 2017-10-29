package jmri.server.json.signalHead;

import static jmri.server.json.JSON.APPEARANCE;
import static jmri.server.json.JSON.APPEARANCE_NAME;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.LIT;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TOKEN_HELD;
import static jmri.server.json.JSON.TYPE;
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
import jmri.server.json.JsonNamedBeanHttpService;

/**
 * JSON HTTP service for {@link jmri.SignalHead}s.
 *
 * @author Randall Wood (C) 2016
 */
public class JsonSignalHeadHttpService extends JsonNamedBeanHttpService {

    public JsonSignalHeadHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, SIGNAL_HEAD);
        SignalHead signalHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);
        ObjectNode data = this.getNamedBean(signalHead, name, type, locale);
        root.put(DATA, data);
        if (signalHead != null) {
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
        }
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        SignalHead signalHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);
        this.postNamedBean(signalHead, data, name, type, locale);
        if (signalHead != null) {
            if (data.path(STATE).isIntegralNumber()) {
                int state = data.path(STATE).asInt();
                if (state == SignalHead.HELD) {
                    signalHead.setHeld(true);                    
                } else {
                    boolean isValid = false;
                    for (int validState : signalHead.getValidStates()) {
                        if (state == validState) {
                            isValid = true;
                            // TODO: completely insulate JSON state from SignalHead state
                            if (signalHead.getHeld()) signalHead.setHeld(false);
                            signalHead.setAppearance(state);
                            break;
                        }
                    }
                    if (!isValid) {
                        throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", SIGNAL_HEAD, state));
                    }
                }
            }
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.getDefault(SignalHeadManager.class).getSystemNameList()) {
            root.add(this.doGet(SIGNAL_HEAD, name, locale));
        }
        return root;
    }
}
