package jmri.server.json.signalhead;

import static jmri.server.json.JSON.APPEARANCE;
import static jmri.server.json.JSON.APPEARANCE_NAME;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.LIT;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TOKEN_HELD;
import static jmri.server.json.signalhead.JsonSignalHead.SIGNAL_HEAD;
import static jmri.server.json.signalhead.JsonSignalHead.SIGNAL_HEADS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNonProvidedNamedBeanHttpService;
import jmri.server.json.JsonRequest;

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
    public JsonNode doGet(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        return doGet(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name), name, type, request);
    }

    @Override
    protected ObjectNode doGet(SignalHead signalHead, String name, String type, JsonRequest request) throws JsonException {
        ObjectNode root = this.getNamedBean(signalHead, name, type, request); // throws JsonException if signalHead == null
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
    public JsonNode doPost(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        SignalHead signalHead = this.postNamedBean(InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name), data, name, type, request);
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
                    throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorUnknownState", SIGNAL_HEAD, state), request.id);
                }
            }
        }
        if (data.path(LIT).isTextual()) {
            signalHead.setLit(data.path(LIT).asBoolean());            
        }
        if (data.path(TOKEN_HELD).isTextual()) {
            signalHead.setHeld(data.path(TOKEN_HELD).asBoolean());            
        }        
        return this.doGet(type, name, data, request);
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, JsonRequest request) throws JsonException {
        ArrayNode array = this.mapper.createArrayNode();
        for (SignalHead head : InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet()) {
            String name = head.getSystemName();
            array.add(this.doGet(SIGNAL_HEAD, name, data, request));
        }
        return message(array, request.id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case SIGNAL_HEAD:
            case SIGNAL_HEADS:
                return doSchema(type,
                        server,
                        "jmri/server/json/signalhead/signalHead-server.json",
                        "jmri/server/json/signalhead/signalHead-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

    @Override
    public SignalHead getNamedBean(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        try {
            if (!data.isEmpty() && !data.isNull()) {
                if (JSON.PUT.equals(request.method)) {
                    doPut(type, name, data, request);
                } else if (JSON.POST.equals(request.method)) {
                    doPost(type, name, data, request);
                }
            }
            return InstanceManager.getDefault(SignalHeadManager.class).getBySystemName(name);
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(request.locale, "ErrorInvalidSystemName", name, type), request.id);
        }
    }
}
