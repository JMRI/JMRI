package jmri.jmris.json;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import org.apache.log4j.Logger;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Throttle;
import jmri.ThrottleListener;
import jmri.jmrit.roster.Roster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class JsonThrottle implements ThrottleListener, PropertyChangeListener {

	private JsonThrottleServer server;
	private String throttleId;
	private Throttle throttle;
	float speedFactor;
	private ObjectMapper mapper;
	static final Logger log = Logger.getLogger(JsonThrottle.class);
	
	public JsonThrottle(String throttleId, JsonNode data, JsonThrottleServer server) throws JmriException {
		this.throttleId = throttleId;
		this.server = server;
		this.mapper = new ObjectMapper();
		this.speedFactor = 1.0f/126.0f;
		boolean result = false;
		if (!data.path("dccAddress").isMissingNode()) {
			result = InstanceManager.throttleManagerInstance().requestThrottle(data.path("dccAddress").asInt(), this);
		} else if (!data.path("id").isMissingNode()) {
        	result = InstanceManager.throttleManagerInstance().requestThrottle(Roster.instance().getEntryForId(data.path("id").asText()), this);
		} else {
			throw new JmriException("No address specified.");
		}
		if (!result) {
			throw new JmriException("Unable to get throttle.");
		}
	}

	public void close() {
		this.throttle.release(this);
		this.throttle.removePropertyChangeListener(this);
	}
	
	public void parseRequest(JsonNode data) {
		if (data.path("eStop").asBoolean(false)) {
			this.throttle.setSpeedSetting(-1);
		} else if (data.path("idle").asBoolean(false)) {
			this.throttle.setSpeedSetting(0);
		} else if (!data.path("speed").isMissingNode()) {
			this.throttle.setSpeedSetting(data.path("speed").asInt()*this.speedFactor);
		} else if (!data.path("forward").isMissingNode()) {
			this.throttle.setIsForward(data.path("forward").asBoolean());
		} else if (!data.path("F0").isMissingNode()) {
			this.throttle.setF0(data.path("F0").asBoolean());
		} else if (!data.path("F1").isMissingNode()) {
			this.throttle.setF1(data.path("F1").asBoolean());
		} else if (!data.path("F2").isMissingNode()) {
			this.throttle.setF2(data.path("F2").asBoolean());
		} else if (!data.path("F3").isMissingNode()) {
			this.throttle.setF3(data.path("F3").asBoolean());
		} else if (!data.path("F4").isMissingNode()) {
			this.throttle.setF4(data.path("F4").asBoolean());
		} else if (!data.path("F5").isMissingNode()) {
			this.throttle.setF5(data.path("F5").asBoolean());
		} else if (!data.path("F6").isMissingNode()) {
			this.throttle.setF6(data.path("F6").asBoolean());
		} else if (!data.path("F7").isMissingNode()) {
			this.throttle.setF7(data.path("F7").asBoolean());
		} else if (!data.path("F8").isMissingNode()) {
			this.throttle.setF8(data.path("F8").asBoolean());
		} else if (!data.path("F9").isMissingNode()) {
			this.throttle.setF9(data.path("F9").asBoolean());
		} else if (!data.path("F10").isMissingNode()) {
			this.throttle.setF10(data.path("F10").asBoolean());
		} else if (!data.path("F11").isMissingNode()) {
			this.throttle.setF11(data.path("F11").asBoolean());
		} else if (!data.path("F12").isMissingNode()) {
			this.throttle.setF12(data.path("F12").asBoolean());
		} else if (!data.path("F13").isMissingNode()) {
			this.throttle.setF13(data.path("F13").asBoolean());
		} else if (!data.path("F14").isMissingNode()) {
			this.throttle.setF14(data.path("F14").asBoolean());
		} else if (!data.path("F15").isMissingNode()) {
			this.throttle.setF15(data.path("F15").asBoolean());
		} else if (!data.path("F16").isMissingNode()) {
			this.throttle.setF16(data.path("F16").asBoolean());
		} else if (!data.path("F17").isMissingNode()) {
			this.throttle.setF17(data.path("F17").asBoolean());
		} else if (!data.path("F18").isMissingNode()) {
			this.throttle.setF18(data.path("F18").asBoolean());
		} else if (!data.path("F19").isMissingNode()) {
			this.throttle.setF19(data.path("F19").asBoolean());
		} else if (!data.path("F20").isMissingNode()) {
			this.throttle.setF20(data.path("F20").asBoolean());
		} else if (!data.path("F21").isMissingNode()) {
			this.throttle.setF21(data.path("F21").asBoolean());
		} else if (!data.path("F22").isMissingNode()) {
			this.throttle.setF22(data.path("F22").asBoolean());
		} else if (!data.path("F23").isMissingNode()) {
			this.throttle.setF23(data.path("F23").asBoolean());
		} else if (!data.path("F24").isMissingNode()) {
			this.throttle.setF24(data.path("F24").asBoolean());
		} else if (!data.path("F25").isMissingNode()) {
			this.throttle.setF25(data.path("F25").asBoolean());
		} else if (!data.path("F26").isMissingNode()) {
			this.throttle.setF26(data.path("F26").asBoolean());
		} else if (!data.path("F27").isMissingNode()) {
			this.throttle.setF27(data.path("F27").asBoolean());
		} else if (!data.path("F28").isMissingNode()) {
			this.throttle.setF28(data.path("F28").asBoolean());
		} else if (!data.path("release").isMissingNode()) {
			this.throttle.release(this);
		} else if (data.path("speedSteps").asInt(0) != 0) {
			((DccThrottle)this.throttle).setSpeedStepMode(data.path("speedSteps").asInt());
			//TODO: set speedFactor
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		ObjectNode data = this.mapper.createObjectNode();
		String property = evt.getPropertyName();
		if (property.equals("SpeedSetting")) {
			data.put("speed", Math.round(((Float)evt.getNewValue()).floatValue() / this.speedFactor));
		} else if (property.equals("IsForward")) {
			data.put("forward", ((Boolean)evt.getNewValue()));
		} else if (property.startsWith("F") && !property.contains("Momentary")) {
			data.put(property, ((Boolean)evt.getNewValue()));
		} else if (property.equals("SpeedSteps")) {
			data.put("speedSteps", ((Integer)evt.getNewValue()));
		}
	}

	@Override
	public void notifyThrottleFound(DccThrottle t) {
		try {
		this.throttle = t;
		ObjectNode data = this.mapper.createObjectNode();
		data.put("dccAddress", this.throttle.getLocoAddress().getNumber());
		if (this.throttle.getRosterEntry() != null) {
			data.put("rosterEntry", this.throttle.getRosterEntry().getId());
		}
		try {
			this.server.sendMessage(throttleId, data);
		} catch (IOException e) {
			this.throttle.setSpeedSetting(0);
			this.throttle.release(this);
			log.warn("Unable to send message, closing connection.", e);
			try {
				this.server.connection.close();
			} catch (IOException e1) {
				log.warn("Unable to close connection.", e);
			}
		}
		} catch (Exception e) {
			log.debug(e,e);
		}
	}

	@Override
	public void notifyFailedThrottleRequest(DccLocoAddress address,
			String reason) {
		try {
			this.server.sendErrorMessage(-1, "Unable to get throttle.");
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
