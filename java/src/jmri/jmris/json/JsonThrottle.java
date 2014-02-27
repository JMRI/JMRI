package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Locale;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Throttle;
import jmri.ThrottleListener;
import static jmri.jmris.json.JSON.ADDRESS;
import static jmri.jmris.json.JSON.ESTOP;
import static jmri.jmris.json.JSON.F;
import static jmri.jmris.json.JSON.FORWARD;
import static jmri.jmris.json.JSON.ID;
import static jmri.jmris.json.JSON.IDLE;
import static jmri.jmris.json.JSON.RELEASE;
import static jmri.jmris.json.JSON.ROSTER_ENTRY;
import static jmri.jmris.json.JSON.SPEED;
import static jmri.jmris.json.JSON.STATUS;
import jmri.jmrit.roster.Roster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonThrottle implements ThrottleListener, PropertyChangeListener {

    private final JsonThrottleServer server;
    private final String throttleId;
    private Throttle throttle;
    private final ObjectMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(JsonThrottle.class);

    @SuppressWarnings("LeakingThisInConstructor")
    public JsonThrottle(String throttleId, JsonNode data, JsonThrottleServer server) throws JmriException {
        this.throttleId = throttleId;
        this.server = server;
        this.mapper = new ObjectMapper();
        boolean result = false;
        if (!data.path(ADDRESS).isMissingNode()) {
            result = InstanceManager.throttleManagerInstance().requestThrottle(data.path(ADDRESS).asInt(), this);
        } else if (!data.path(ID).isMissingNode()) {
            try {
                result = InstanceManager.throttleManagerInstance().requestThrottle(Roster.instance().getEntryForId(data.path(ID).asText()), this);
            } catch (NullPointerException ex) {
                this.sendErrorMessage(-100, Bundle.getMessage(this.server.connection.getLocale(),"ErrorThrottleRosterEntry", data.path(ID).asText()));
                throw new JmriException("Roster entry " + data.path(ID).asText() + " does not exist."); // NOI18N
            }
        } else {
            this.sendErrorMessage(-101, Bundle.getMessage(this.server.connection.getLocale(),"ErrorThrottleNoAddress"));
            throw new JmriException("No address specified."); // NOI18N
        }
        if (!result) {
            // notify end user when notifyFailedThrottleRequest is called
            throw new JmriException("Unable to get throttle."); // NOI18N
        }
    }

    public void close() {
        if (this.throttle != null) {
            this.throttle.setSpeedSetting(0);
            this.release();
        }
    }

    public void release() {
        if (this.throttle != null) {
            this.throttle.release(this);
            this.throttle.removePropertyChangeListener(this);
            this.throttle = null;
            this.sendMessage(this.mapper.createObjectNode().putNull(RELEASE));
        }
    }

    public void parseRequest(Locale locale, JsonNode data) {
        if (data.path(ESTOP).asBoolean(false)) {
            this.throttle.setSpeedSetting(-1);
        } else if (data.path(IDLE).asBoolean(false)) {
            this.throttle.setSpeedSetting(0);
        } else if (!data.path(SPEED).isMissingNode()) {
            this.throttle.setSpeedSetting((float) data.path(SPEED).asDouble());
        } else if (!data.path(FORWARD).isMissingNode()) {
            this.throttle.setIsForward(data.path(FORWARD).asBoolean());
        } else if (!data.path(Throttle.F0).isMissingNode()) {
            this.throttle.setF0(data.path(Throttle.F0).asBoolean());
        } else if (!data.path(Throttle.F1).isMissingNode()) {
            this.throttle.setF1(data.path(Throttle.F1).asBoolean());
        } else if (!data.path(Throttle.F2).isMissingNode()) {
            this.throttle.setF2(data.path(Throttle.F2).asBoolean());
        } else if (!data.path(Throttle.F3).isMissingNode()) {
            this.throttle.setF3(data.path(Throttle.F3).asBoolean());
        } else if (!data.path(Throttle.F4).isMissingNode()) {
            this.throttle.setF4(data.path(Throttle.F4).asBoolean());
        } else if (!data.path(Throttle.F5).isMissingNode()) {
            this.throttle.setF5(data.path(Throttle.F5).asBoolean());
        } else if (!data.path(Throttle.F6).isMissingNode()) {
            this.throttle.setF6(data.path(Throttle.F6).asBoolean());
        } else if (!data.path(Throttle.F7).isMissingNode()) {
            this.throttle.setF7(data.path(Throttle.F7).asBoolean());
        } else if (!data.path(Throttle.F8).isMissingNode()) {
            this.throttle.setF8(data.path(Throttle.F8).asBoolean());
        } else if (!data.path(Throttle.F9).isMissingNode()) {
            this.throttle.setF9(data.path(Throttle.F9).asBoolean());
        } else if (!data.path(Throttle.F10).isMissingNode()) {
            this.throttle.setF10(data.path(Throttle.F10).asBoolean());
        } else if (!data.path(Throttle.F11).isMissingNode()) {
            this.throttle.setF11(data.path(Throttle.F11).asBoolean());
        } else if (!data.path(Throttle.F12).isMissingNode()) {
            this.throttle.setF12(data.path(Throttle.F12).asBoolean());
        } else if (!data.path(Throttle.F13).isMissingNode()) {
            this.throttle.setF13(data.path(Throttle.F13).asBoolean());
        } else if (!data.path(Throttle.F14).isMissingNode()) {
            this.throttle.setF14(data.path(Throttle.F14).asBoolean());
        } else if (!data.path(Throttle.F15).isMissingNode()) {
            this.throttle.setF15(data.path(Throttle.F15).asBoolean());
        } else if (!data.path(Throttle.F16).isMissingNode()) {
            this.throttle.setF16(data.path(Throttle.F16).asBoolean());
        } else if (!data.path(Throttle.F17).isMissingNode()) {
            this.throttle.setF17(data.path(Throttle.F17).asBoolean());
        } else if (!data.path(Throttle.F18).isMissingNode()) {
            this.throttle.setF18(data.path(Throttle.F18).asBoolean());
        } else if (!data.path(Throttle.F19).isMissingNode()) {
            this.throttle.setF19(data.path(Throttle.F19).asBoolean());
        } else if (!data.path(Throttle.F20).isMissingNode()) {
            this.throttle.setF20(data.path(Throttle.F20).asBoolean());
        } else if (!data.path(Throttle.F21).isMissingNode()) {
            this.throttle.setF21(data.path(Throttle.F21).asBoolean());
        } else if (!data.path(Throttle.F22).isMissingNode()) {
            this.throttle.setF22(data.path(Throttle.F22).asBoolean());
        } else if (!data.path(Throttle.F23).isMissingNode()) {
            this.throttle.setF23(data.path(Throttle.F23).asBoolean());
        } else if (!data.path(Throttle.F24).isMissingNode()) {
            this.throttle.setF24(data.path(Throttle.F24).asBoolean());
        } else if (!data.path(Throttle.F25).isMissingNode()) {
            this.throttle.setF25(data.path(Throttle.F25).asBoolean());
        } else if (!data.path(Throttle.F26).isMissingNode()) {
            this.throttle.setF26(data.path(Throttle.F26).asBoolean());
        } else if (!data.path(Throttle.F27).isMissingNode()) {
            this.throttle.setF27(data.path(Throttle.F27).asBoolean());
        } else if (!data.path(Throttle.F28).isMissingNode()) {
            this.throttle.setF28(data.path(Throttle.F28).asBoolean());
        } else if (!data.path(RELEASE).isMissingNode()) {
            this.server.release(throttleId);
        } else if (!data.path(STATUS).isMissingNode()) {
            this.sendStatus();
        }
    }

    public void sendMessage(ObjectNode data) {
        try {
            this.server.sendMessage(throttleId, data);
        } catch (IOException ex) {
            this.close();
            log.warn("Unable to send message, closing connection.", ex);
            try {
                this.server.connection.close();
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
    public void notifyThrottleFound(DccThrottle t) {
        try {
            this.throttle = t;
            t.addPropertyChangeListener(this);
            this.sendStatus();
        } catch (Exception e) {
            log.debug(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void notifyFailedThrottleRequest(DccLocoAddress address, String reason) {
        this.sendErrorMessage(-102, Bundle.getMessage(this.server.connection.getLocale(), "ErrorThrottleRequestFailed", address, reason));
    }

    private void sendErrorMessage(int code, String message) {
        try {
            this.server.sendErrorMessage(code, message);
        } catch (IOException e) {
            try {
                this.server.connection.close();
            } catch (IOException e1) {
                log.warn("Unable to close connection.", e);
            }
            log.warn("Unable to send message, closing connection.", e);
        }
    }

    private void sendStatus() {
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
        if (this.throttle.getRosterEntry() != null) {
            data.put(ROSTER_ENTRY, this.throttle.getRosterEntry().getId());
        }
        this.sendMessage(data);
    }
}
