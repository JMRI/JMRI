package jmri.server.json.signalmast;

import static jmri.server.json.JSON.ASPECT;
import static jmri.server.json.JSON.ASPECT_DARK;
import static jmri.server.json.JSON.ASPECT_HELD;
import static jmri.server.json.JSON.ASPECT_UNKNOWN;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.LIT;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TOKEN_HELD;
import static jmri.server.json.signalmast.JsonSignalMast.SIGNAL_MAST;
import static jmri.server.json.signalmast.JsonSignalMast.SIGNAL_MASTS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.ProvidingManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;

/**
 * JSON HTTP service for {@link jmri.SignalMast}s.
 *
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonSignalMastHttpService extends JsonNamedBeanHttpService<SignalMast> {

    public JsonSignalMastHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(SignalMast signalMast, String name, String type, Locale locale, int id) throws JsonException {
        ObjectNode root = this.getNamedBean(signalMast, name, type, locale, id); // throws if signalMast is null
        ObjectNode data = root.with(DATA);
        String aspect = signalMast.getAspect();
        if (aspect == null) {
            aspect = ASPECT_UNKNOWN; //if null, set aspect to "Unknown"
        }
        data.put(ASPECT, aspect);
        data.put(LIT, signalMast.getLit());
        data.put(TOKEN_HELD, signalMast.getHeld());
        // state is appearance, plus flags for held and dark statuses
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
    public ObjectNode doPost(SignalMast signalMast, String name, String type, JsonNode data, Locale locale, int id) throws JsonException {
        if (data.path(STATE).isTextual()) {
            String aspect = data.path(STATE).asText();
            if (aspect.equals(ASPECT_HELD)) {
                signalMast.setHeld(true);
            } else if (signalMast.getValidAspects().contains(aspect)) {
                if (signalMast.getHeld()) {
                    signalMast.setHeld(false);
                }
                if (signalMast.getAspect() == null || !signalMast.getAspect().equals(aspect)) {
                    signalMast.setAspect(aspect);
                }
            } else {
                throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", SIGNAL_MAST, aspect), id);
            }
        }
        return this.doGet(signalMast, name, type, locale, id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case SIGNAL_MAST:
            case SIGNAL_MASTS:
                return doSchema(type,
                        server,
                        "jmri/server/json/signalmast/signalMast-server.json",
                        "jmri/server/json/signalmast/signalMast-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }

    @Override
    protected String getType() {
        return SIGNAL_MAST;
    }

    @Override
    protected ProvidingManager<SignalMast> getManager() {
        return InstanceManager.getDefault(SignalMastManager.class);
    }
}
