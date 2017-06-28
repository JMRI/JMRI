package jmri.jmris.json;

import static jmri.server.json.JSON.ASPECT_DARK;
import static jmri.server.json.JSON.ASPECT_HELD;
import static jmri.server.json.JSON.ASPECT_UNKNOWN;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JsonException.CODE;
import static jmri.server.json.JsonException.ERROR;
import static jmri.server.json.JsonException.MESSAGE;
import static jmri.server.json.signalMast.JsonSignalMast.SIGNAL_MAST;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalMast;
import jmri.jmris.AbstractSignalMastServer;
import jmri.jmris.JmriConnection;
import jmri.server.json.JsonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON Web Socket interface between the JMRI SignalMast manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @deprecated since 4.5.6
 */
@Deprecated
public class JsonSignalMastServer extends AbstractSignalMastServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    private final static Logger log = LoggerFactory.getLogger(JsonSignalMastServer.class);

    public JsonSignalMastServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String signalMastName, String status) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, SIGNAL_MAST);
        ObjectNode data = root.putObject(DATA);
        data.put(NAME, signalMastName);
        data.put(STATE, status);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void sendErrorStatus(String signalMastName) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(ERROR);
        data.put(NAME, signalMastName);
        data.put(CODE, -1);
        data.put(MESSAGE, Bundle.getMessage("ErrorObject", SIGNAL_MAST, signalMastName));
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException, JsonException {
        this.parseRequest(Locale.getDefault(), this.mapper.readTree(statusString).path(DATA));
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        String state = data.path(STATE).asText();
        if ("".equals(state)) {  //if not passed, retrieve current and respond
            SignalMast sm = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(name);
            try {
                state = sm.getAspect();
                if (state == null) {
                    state = ASPECT_UNKNOWN; //if null, set state to "Unknown"   
                }                
                if ((sm.getHeld()) && (sm.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD) != null)) {
                    state = ASPECT_HELD;
                } else if ((!sm.getLit()) && (sm.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DARK) != null)) {
                    state = ASPECT_DARK;
                }
                this.sendStatus(name, state);
            } catch (IOException ex) {
                this.sendErrorStatus(name);
            } catch (NullPointerException e) {
                log.error("Unable to get signalMast [{}].", name);
                throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SIGNAL_MAST, name));
            }
        } else { //else set the aspect to the state passed in
            this.setSignalMastAspect(name, state);
        }
        this.addSignalMastToList(name);
    }
}
