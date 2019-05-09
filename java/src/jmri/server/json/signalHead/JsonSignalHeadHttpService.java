package jmri.server.json.signalHead;

import static jmri.server.json.JSON.APPEARANCE;
import static jmri.server.json.JSON.APPEARANCE_NAME;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.LIT;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TOKEN_HELD;
import static jmri.server.json.signalHead.JsonSignalHead.SIGNAL_HEAD;
import static jmri.server.json.signalHead.JsonSignalHead.SIGNAL_HEADS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNonProvidedNamedBeanHttpService;

/**
 * JSON HTTP service for {@link jmri.SignalHead}s.
 *
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonSignalHeadHttpService extends JsonNonProvidedNamedBeanHttpService<SignalHead> {

    public JsonSignalHeadHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, Locale locale) throws JsonException {
        return doGet(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name), name, type, locale);
    }

    @Override
    protected ObjectNode doGet(SignalHead signalHead, String name, String type, Locale locale) throws JsonException {
        ObjectNode root = this.getNamedBean(signalHead, name, type, locale); // throws JsonException if signalHead == null
        ObjectNode data = root.with(DATA);
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
        SignalHead signalHead = this.postNamedBean(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name), data, name, type, locale);
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
                        if (signalHead.getHeld()) {
                            signalHead.setHeld(false);
                        }
                        signalHead.setAppearance(state);
                        break;
                    }
                }
                if (!isValid) {
                    throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", SIGNAL_HEAD, state));
                }
            }
        }
        return this.doGet(type, name, data, locale);
    }

    @Override
    public ArrayNode doGetList(String type, JsonNode data, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (SignalHead head : InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet()) {
            String name = head.getSystemName();
            root.add(this.doGet(SIGNAL_HEAD, name, data, locale));
        }
        return root;
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale) throws JsonException {
        switch (type) {
            case SIGNAL_HEAD:
            case SIGNAL_HEADS:
                return doSchema(type,
                        server,
                        "jmri/server/json/signalHead/signalHead-server.json",
                        "jmri/server/json/signalHead/signalHead-client.json");
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }
}
