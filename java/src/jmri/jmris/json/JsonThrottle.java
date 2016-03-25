package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Throttle;
import jmri.ThrottleListener;
import static jmri.jmris.json.JSON.ADDRESS;
import static jmri.jmris.json.JSON.CLIENTS;
import static jmri.jmris.json.JSON.ESTOP;
import static jmri.jmris.json.JSON.F;
import static jmri.jmris.json.JSON.FORWARD;
import static jmri.jmris.json.JSON.ID;
import static jmri.jmris.json.JSON.IDLE;
import static jmri.jmris.json.JSON.IS_LONG_ADDRESS;
import static jmri.jmris.json.JSON.RELEASE;
import static jmri.jmris.json.JSON.ROSTER_ENTRY;
import static jmri.jmris.json.JSON.SPEED;
import static jmri.jmris.json.JSON.SPEED_STEPS;
import static jmri.jmris.json.JSON.STATUS;
import static jmri.jmris.json.JSON.THROTTLE;
import jmri.jmrit.roster.Roster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Randall Wood
 * @deprecated since 4.3.4
 */
@Deprecated
public class JsonThrottle implements ThrottleListener, PropertyChangeListener {

    private final ArrayList<JsonThrottleServer> servers = new ArrayList<>();
    private Throttle throttle;
    private int speedSteps = 1;
    private DccLocoAddress address = null;
    private final ObjectMapper mapper = new ObjectMapper();
    private static HashMap<DccLocoAddress, JsonThrottle> throttles = null;
    private static final Logger log = LoggerFactory.getLogger(JsonThrottle.class);

    private JsonThrottle(DccLocoAddress address, JsonThrottleServer server) {
        this.address = address;
        this.servers.add(server);
    }

    /**
     * Creates a new JsonThrottle or returns an existing one if the request is
     * for an existing throttle.
     *
     * data can contain either a string {@link JSON#ID} node containing the ID
     * of a {@link jmri.jmrit.roster.RosterEntry} or an integer
     * {@link JSON#ADDRESS} node. If data contains an ADDRESS, the ID node is
     * ignored. The ADDRESS may be accompanied by a boolean
     * {@link JSON#IS_LONG_ADDRESS} node specifying the type of address, if
     * IS_LONG_ADDRESS is not specified, the inverse of {@link jmri.ThrottleManager#canBeShortAddress(int)
     * } is used as the "best guess" of the address length.
     *
     * @param throttleId The client's identity token for this throttle
     * @param data       JSON object containing either an ADDRESS or an ID
     * @param server     The server requesting this throttle on behalf of a
     *                   client
     * @return The throttle
     * @throws JmriException
     * @throws IOException
     */
    public static JsonThrottle getThrottle(String throttleId, JsonNode data, JsonThrottleServer server) throws JmriException, IOException {
        DccLocoAddress address = null;
        if (!data.path(ADDRESS).isMissingNode()) {
            if (InstanceManager.throttleManagerInstance().canBeLongAddress(data.path(ADDRESS).asInt())
                    || InstanceManager.throttleManagerInstance().canBeShortAddress(data.path(ADDRESS).asInt())) {
                address = new DccLocoAddress(data.path(ADDRESS).asInt(),
                        data.path(IS_LONG_ADDRESS).asBoolean(!InstanceManager.throttleManagerInstance().canBeShortAddress(data.path(ADDRESS).asInt())));
            } else {
                server.sendErrorMessage(-103, Bundle.getMessage(server.connection.getLocale(), "ErrorThrottleInvalidAddress", data.path(ADDRESS).asInt()));
                throw new JmriException("Address " + data.path(ADDRESS).asInt() + " is not a valid address."); // NOI18N
            }
        } else if (!data.path(ID).isMissingNode()) {
            try {
                address = Roster.instance().getEntryForId(data.path(ID).asText()).getDccLocoAddress();
            } catch (NullPointerException ex) {
                server.sendErrorMessage(-100, Bundle.getMessage(server.connection.getLocale(), "ErrorThrottleRosterEntry", data.path(ID).asText()));
                throw new JmriException("Roster entry " + data.path(ID).asText() + " does not exist."); // NOI18N
            }
        } else {
            server.sendErrorMessage(-101, Bundle.getMessage(server.connection.getLocale(), "ErrorThrottleNoAddress"));
            throw new JmriException("No address specified."); // NOI18N
        }
        if (JsonThrottle.throttles == null) {
            JsonThrottle.throttles = new HashMap<>();
        }
        if (JsonThrottle.throttles.containsKey(address)) {
            JsonThrottle throttle = JsonThrottle.throttles.get(address);
            throttle.servers.add(server);
            throttle.sendMessage(throttle.mapper.createObjectNode().put(CLIENTS, throttle.servers.size()));
            return throttle;
        } else {
            JsonThrottle throttle = new JsonThrottle(address, server);
            if (!InstanceManager.throttleManagerInstance().requestThrottle(address, throttle)) {
                throw new JmriException("Error getting throttle for " + address); // NOI18N
            }
            JsonThrottle.throttles.put(address, throttle);
            return throttle;
        }
    }

    public void close(JsonThrottleServer server, boolean notifyClient) {
        if (this.throttle != null) {
            if (this.servers.size() == 1) {
                this.throttle.setSpeedSetting(0);
            }
            this.release(server, notifyClient);
        }
    }

    public void release(JsonThrottleServer server) {
        this.release(server, true);
    }

    private void release(JsonThrottleServer server, boolean notifyClient) {
        if (this.throttle != null) {
            if (this.servers.size() == 1) {
                this.throttle.release(this);
                this.throttle.removePropertyChangeListener(this);
                this.throttle = null;
            }
            if (notifyClient) {
                this.sendMessage(this.mapper.createObjectNode().putNull(RELEASE), server);
            }
        }
        this.servers.remove(server);
        if (this.servers.isEmpty()) {
            // Release address-based reference to this throttle if there are no servers using it
            // so that when the server releases its reference, this throttle can be garbage collected
            JsonThrottle.throttles.remove(this.address);
        } else {
            this.sendMessage(this.mapper.createObjectNode().put(CLIENTS, this.servers.size()));
        }
    }

    public void parseRequest(Locale locale, JsonNode data, JsonThrottleServer server) {
        Iterator<Entry<String, JsonNode>> nodeIterator = data.fields();
        while (nodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = nodeIterator.next();
            String k = entry.getKey();
            JsonNode v = entry.getValue();
            switch (k) {
                case ESTOP:
                    this.throttle.setSpeedSetting(-1);
                    return; // stop processing any commands that may conflict with ESTOP
                case IDLE:
                    this.throttle.setSpeedSetting(0);
                    break;
                case SPEED:
                    this.throttle.setSpeedSetting((float) v.asDouble());
                    break;
                case FORWARD:
                    this.throttle.setIsForward(v.asBoolean());
                    break;
                case Throttle.F0:
                    this.throttle.setF0(v.asBoolean());
                    break;
                case Throttle.F1:
                    this.throttle.setF1(v.asBoolean());
                    break;
                case Throttle.F2:
                    this.throttle.setF2(v.asBoolean());
                    break;
                case Throttle.F3:
                    this.throttle.setF3(v.asBoolean());
                    break;
                case Throttle.F4:
                    this.throttle.setF4(v.asBoolean());
                    break;
                case Throttle.F5:
                    this.throttle.setF5(v.asBoolean());
                    break;
                case Throttle.F6:
                    this.throttle.setF6(v.asBoolean());
                    break;
                case Throttle.F7:
                    this.throttle.setF7(v.asBoolean());
                    break;
                case Throttle.F8:
                    this.throttle.setF8(v.asBoolean());
                    break;
                case Throttle.F9:
                    this.throttle.setF9(v.asBoolean());
                    break;
                case Throttle.F10:
                    this.throttle.setF10(v.asBoolean());
                    break;
                case Throttle.F11:
                    this.throttle.setF11(v.asBoolean());
                    break;
                case Throttle.F12:
                    this.throttle.setF12(v.asBoolean());
                    break;
                case Throttle.F13:
                    this.throttle.setF13(v.asBoolean());
                    break;
                case Throttle.F14:
                    this.throttle.setF14(v.asBoolean());
                    break;
                case Throttle.F15:
                    this.throttle.setF15(v.asBoolean());
                    break;
                case Throttle.F16:
                    this.throttle.setF16(v.asBoolean());
                    break;
                case Throttle.F17:
                    this.throttle.setF17(v.asBoolean());
                    break;
                case Throttle.F18:
                    this.throttle.setF18(v.asBoolean());
                    break;
                case Throttle.F19:
                    this.throttle.setF19(v.asBoolean());
                    break;
                case Throttle.F20:
                    this.throttle.setF20(v.asBoolean());
                    break;
                case Throttle.F21:
                    this.throttle.setF21(v.asBoolean());
                    break;
                case Throttle.F22:
                    this.throttle.setF22(v.asBoolean());
                    break;
                case Throttle.F23:
                    this.throttle.setF23(v.asBoolean());
                    break;
                case Throttle.F24:
                    this.throttle.setF24(v.asBoolean());
                    break;
                case Throttle.F25:
                    this.throttle.setF25(v.asBoolean());
                    break;
                case Throttle.F26:
                    this.throttle.setF26(v.asBoolean());
                    break;
                case Throttle.F27:
                    this.throttle.setF27(v.asBoolean());
                    break;
                case Throttle.F28:
                    this.throttle.setF28(v.asBoolean());
                    break;
                case RELEASE:
                    server.release(this);
                    break;
                case STATUS:
                    this.sendStatus(server);
                    break;
                case THROTTLE:
                default:
                    // no action for throttle item; it always exists
                    // silently ignore unknown or unexpected items, since a
                    // following item may be an ESTOP and we always want to
                    // catch those
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void sendMessage(ObjectNode data) {
        for (JsonThrottleServer server : (ArrayList<JsonThrottleServer>) this.servers.clone()) {
            this.sendMessage(data, server);
        }
    }

    public void sendMessage(ObjectNode data, JsonThrottleServer server) {
        try {
            server.sendMessage(this, data);
        } catch (IOException ex) {
            this.close(server, false);
            log.warn("Unable to send message, closing connection: {}", ex.getMessage());
            try {
                server.connection.close();
            } catch (IOException e1) {
                log.warn("Unable to close connection.", e1);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        ObjectNode data = this.mapper.createObjectNode();
        String property = evt.getPropertyName();
        if (property.equals("SpeedSetting")) { // NOI18N
            data.put(SPEED, ((Float) evt.getNewValue()).floatValue());
        } else if (property.equals("IsForward")) { // NOI18N
            data.put(FORWARD, ((Boolean) evt.getNewValue()));
        } else if (property.startsWith(F) && !property.contains("Momentary")) { // NOI18N
            data.put(property, ((Boolean) evt.getNewValue()));
        }
        this.sendMessage(data);
    }

    @Override
    public void notifyThrottleFound(DccThrottle throttle) {
        try {
            this.throttle = throttle;
            throttle.addPropertyChangeListener(this);
            switch (throttle.getSpeedStepMode()) {
                case DccThrottle.SpeedStepMode14:
                    this.speedSteps = 14;
                    break;
                case DccThrottle.SpeedStepMode27:
                    this.speedSteps = 27;
                    break;
                case DccThrottle.SpeedStepMode28:
                case DccThrottle.SpeedStepMode28Mot:
                    this.speedSteps = 28;
                    break;
                case DccThrottle.SpeedStepMode128:
                default:
                    this.speedSteps = 126;
                    break;
            }
            this.sendStatus();
        } catch (Exception e) {
            log.debug(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void notifyFailedThrottleRequest(DccLocoAddress address, String reason) {
        for (JsonThrottleServer server : (ArrayList<JsonThrottleServer>) this.servers.clone()) {
            this.sendErrorMessage(-102, Bundle.getMessage(server.connection.getLocale(), "ErrorThrottleRequestFailed", address, reason), server);
            server.release(this);
        }
    }

    private void sendErrorMessage(int code, String message, JsonThrottleServer server) {
        try {
            server.sendErrorMessage(code, message);
        } catch (IOException e) {
            log.warn("Unable to send message, closing connection. ", e);
            try {
                server.connection.close();
            } catch (IOException e1) {
                log.warn("Unable to close connection.", e1);
            }
        }
    }

    private void sendStatus() {
        if (this.throttle != null) {
            this.sendMessage(this.getStatus());
        }
    }

    protected void sendStatus(JsonThrottleServer server) {
        if (this.throttle != null) {
            this.sendMessage(this.getStatus(), server);
        }
    }

    private ObjectNode getStatus() {
        ObjectNode data = this.mapper.createObjectNode();
        data.put(ADDRESS, this.throttle.getLocoAddress().getNumber());
        data.put(SPEED, this.throttle.getSpeedSetting());
        data.put(FORWARD, this.throttle.getIsForward());
        data.put(Throttle.F0, this.throttle.getF0());
        data.put(Throttle.F1, this.throttle.getF1());
        data.put(Throttle.F2, this.throttle.getF2());
        data.put(Throttle.F3, this.throttle.getF3());
        data.put(Throttle.F4, this.throttle.getF4());
        data.put(Throttle.F5, this.throttle.getF5());
        data.put(Throttle.F6, this.throttle.getF6());
        data.put(Throttle.F7, this.throttle.getF7());
        data.put(Throttle.F8, this.throttle.getF8());
        data.put(Throttle.F9, this.throttle.getF9());
        data.put(Throttle.F10, this.throttle.getF10());
        data.put(Throttle.F11, this.throttle.getF11());
        data.put(Throttle.F12, this.throttle.getF12());
        data.put(Throttle.F13, this.throttle.getF13());
        data.put(Throttle.F14, this.throttle.getF14());
        data.put(Throttle.F15, this.throttle.getF15());
        data.put(Throttle.F16, this.throttle.getF16());
        data.put(Throttle.F17, this.throttle.getF17());
        data.put(Throttle.F18, this.throttle.getF18());
        data.put(Throttle.F19, this.throttle.getF19());
        data.put(Throttle.F20, this.throttle.getF20());
        data.put(Throttle.F21, this.throttle.getF21());
        data.put(Throttle.F22, this.throttle.getF22());
        data.put(Throttle.F23, this.throttle.getF23());
        data.put(Throttle.F24, this.throttle.getF24());
        data.put(Throttle.F25, this.throttle.getF25());
        data.put(Throttle.F26, this.throttle.getF26());
        data.put(Throttle.F27, this.throttle.getF27());
        data.put(Throttle.F28, this.throttle.getF28());
        data.put(SPEED_STEPS, this.speedSteps);
        data.put(CLIENTS, this.servers.size());
        if (this.throttle.getRosterEntry() != null) {
            data.put(ROSTER_ENTRY, this.throttle.getRosterEntry().getId());
        }
        return data;
    }
}
