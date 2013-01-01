package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Throttle;
import jmri.ThrottleListener;
import static jmri.jmris.json.JSON.*;
import jmri.jmrit.roster.Roster;
import org.apache.log4j.Logger;

public class JsonThrottle implements ThrottleListener, PropertyChangeListener {

    private JsonThrottleServer server;
    private String throttleId;
    private Throttle throttle;
    private ObjectMapper mapper;
    static final Logger log = Logger.getLogger(JsonThrottle.class);

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
                this.sendErrorMessage(-100, Bundle.getMessage("ErrorThrottleRosterEntry", data.path(ID).asText()));
                throw new JmriException("Roster entry " + data.path(ID).asText() + " does not exist."); // NOI18N
            }
        } else {
            this.sendErrorMessage(-101, Bundle.getMessage("ErrorThrottleNoAddress"));
            throw new JmriException("No address specified."); // NOI18N
        }
        if (!result) {
            // notify end user when notifyFailedThrottleRequest is called
            throw new JmriException("Unable to get throttle."); // NOI18N
        }
    }

    public void close() {
        this.throttle.release(this);
        this.throttle.removePropertyChangeListener(this);
    }

    public void parseRequest(JsonNode data) {
        if (data.path(ESTOP).asBoolean(false)) {
            this.throttle.setSpeedSetting(-1);
        } else if (data.path(IDLE).asBoolean(false)) {
            this.throttle.setSpeedSetting(0);
        } else if (!data.path(SPEED).isMissingNode()) {
            this.throttle.setSpeedSetting((float) data.path(SPEED).asDouble());
        } else if (!data.path(FORWARD).isMissingNode()) {
            this.throttle.setIsForward(data.path(FORWARD).asBoolean());
        } else if (!data.path(F0).isMissingNode()) {
            this.throttle.setF0(data.path(F0).asBoolean());
        } else if (!data.path(F1).isMissingNode()) {
            this.throttle.setF1(data.path(F1).asBoolean());
        } else if (!data.path(F2).isMissingNode()) {
            this.throttle.setF2(data.path(F2).asBoolean());
        } else if (!data.path(F3).isMissingNode()) {
            this.throttle.setF3(data.path(F3).asBoolean());
        } else if (!data.path(F4).isMissingNode()) {
            this.throttle.setF4(data.path(F4).asBoolean());
        } else if (!data.path(F5).isMissingNode()) {
            this.throttle.setF5(data.path(F5).asBoolean());
        } else if (!data.path(F6).isMissingNode()) {
            this.throttle.setF6(data.path(F6).asBoolean());
        } else if (!data.path(F7).isMissingNode()) {
            this.throttle.setF7(data.path(F7).asBoolean());
        } else if (!data.path(F8).isMissingNode()) {
            this.throttle.setF8(data.path(F8).asBoolean());
        } else if (!data.path(F9).isMissingNode()) {
            this.throttle.setF9(data.path(F9).asBoolean());
        } else if (!data.path(F10).isMissingNode()) {
            this.throttle.setF10(data.path(F10).asBoolean());
        } else if (!data.path(F11).isMissingNode()) {
            this.throttle.setF11(data.path(F11).asBoolean());
        } else if (!data.path(F12).isMissingNode()) {
            this.throttle.setF12(data.path(F12).asBoolean());
        } else if (!data.path(F13).isMissingNode()) {
            this.throttle.setF13(data.path(F13).asBoolean());
        } else if (!data.path(F14).isMissingNode()) {
            this.throttle.setF14(data.path(F14).asBoolean());
        } else if (!data.path(F15).isMissingNode()) {
            this.throttle.setF15(data.path(F15).asBoolean());
        } else if (!data.path(F16).isMissingNode()) {
            this.throttle.setF16(data.path(F16).asBoolean());
        } else if (!data.path(F17).isMissingNode()) {
            this.throttle.setF17(data.path(F17).asBoolean());
        } else if (!data.path(F18).isMissingNode()) {
            this.throttle.setF18(data.path(F18).asBoolean());
        } else if (!data.path(F19).isMissingNode()) {
            this.throttle.setF19(data.path(F19).asBoolean());
        } else if (!data.path(F20).isMissingNode()) {
            this.throttle.setF20(data.path(F20).asBoolean());
        } else if (!data.path(F21).isMissingNode()) {
            this.throttle.setF21(data.path(F21).asBoolean());
        } else if (!data.path(F22).isMissingNode()) {
            this.throttle.setF22(data.path(F22).asBoolean());
        } else if (!data.path(F23).isMissingNode()) {
            this.throttle.setF23(data.path(F23).asBoolean());
        } else if (!data.path(F24).isMissingNode()) {
            this.throttle.setF24(data.path(F24).asBoolean());
        } else if (!data.path(F25).isMissingNode()) {
            this.throttle.setF25(data.path(F25).asBoolean());
        } else if (!data.path(F26).isMissingNode()) {
            this.throttle.setF26(data.path(F26).asBoolean());
        } else if (!data.path(F27).isMissingNode()) {
            this.throttle.setF27(data.path(F27).asBoolean());
        } else if (!data.path(F28).isMissingNode()) {
            this.throttle.setF28(data.path(F28).asBoolean());
        } else if (!data.path(RELEASE).isMissingNode()) {
            this.throttle.release(this);
            this.sendMessage(this.mapper.createObjectNode().putNull(RELEASE));
        }
    }

    public void sendMessage(ObjectNode data) {
        try {
            this.server.sendMessage(throttleId, data);
        } catch (IOException ex) {
            this.throttle.setSpeedSetting(0);
            this.throttle.release(this);
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
            ObjectNode data = this.mapper.createObjectNode();
            data.put(ADDRESS, this.throttle.getLocoAddress().getNumber());
            data.put(SPEED, this.throttle.getSpeedSetting());
            data.put(FORWARD, this.throttle.getIsForward());
            data.put(F0, this.throttle.getF0());
            data.put(F1, this.throttle.getF1());
            data.put(F2, this.throttle.getF2());
            data.put(F3, this.throttle.getF3());
            data.put(F4, this.throttle.getF4());
            data.put(F5, this.throttle.getF5());
            data.put(F6, this.throttle.getF6());
            data.put(F7, this.throttle.getF7());
            data.put(F8, this.throttle.getF8());
            data.put(F9, this.throttle.getF9());
            data.put(F10, this.throttle.getF10());
            data.put(F11, this.throttle.getF11());
            data.put(F12, this.throttle.getF12());
            data.put(F13, this.throttle.getF13());
            data.put(F14, this.throttle.getF14());
            data.put(F15, this.throttle.getF15());
            data.put(F16, this.throttle.getF16());
            data.put(F17, this.throttle.getF17());
            data.put(F18, this.throttle.getF18());
            data.put(F19, this.throttle.getF19());
            data.put(F20, this.throttle.getF20());
            data.put(F21, this.throttle.getF21());
            data.put(F22, this.throttle.getF22());
            data.put(F23, this.throttle.getF23());
            data.put(F24, this.throttle.getF24());
            data.put(F25, this.throttle.getF25());
            data.put(F26, this.throttle.getF26());
            data.put(F27, this.throttle.getF27());
            data.put(F28, this.throttle.getF28());
            if (this.throttle.getRosterEntry() != null) {
                data.put(ROSTER_ENTRY, this.throttle.getRosterEntry().getId());
            }
            this.sendMessage(data);
        } catch (Exception e) {
            log.debug(e, e);
        }
    }

    @Override
    public void notifyFailedThrottleRequest(DccLocoAddress address, String reason) {
        this.sendErrorMessage(-102, Bundle.getMessage("ErrorThrottleRequestFailed", address, reason));
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
}
