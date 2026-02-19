package jmri.jmrix.mqtt;

import javax.annotation.Nonnull;
import jmri.implementation.*;

/**
 * SignalMast implemented via MQTT messages
 * <p>
 * System name specifies the creation information:
 * <pre>
 * IF$mqm:basic:one-searchlight($0001)
 * </pre> The name is a colon-separated series of terms:
 * <ul>
 * <li>IF$mqm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>($0001) - small ordinal number for telling various signal masts apart
 * apart
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2021
 */
public class MqttSignalMast extends AbstractSignalMast implements MqttEventListener {

    public MqttSignalMast(String systemName, String userName) {
        super(systemName, userName);
        configureFromName(systemName);
        sendTopic = makeSendTopic(systemName);
        mqttAdapter = jmri.InstanceManager.getDefault(MqttSystemConnectionMemo.class).getMqttAdapter();
        mqttAdapter.subscribe(sendTopic, this);  // receive back on send topic
    }

    public MqttSignalMast(String systemName) {
        super(systemName);
        configureFromName(systemName);
        sendTopic = makeSendTopic(systemName);
        mqttAdapter = jmri.InstanceManager.getDefault(MqttSystemConnectionMemo.class).getMqttAdapter();
        mqttAdapter.subscribe(sendTopic, this);  // receive back on send topic
    }

    @Nonnull
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "MS_PKGPROTECT",
            justification = "Public accessibility for scripts that update the prefixes")
    public static String sendTopicPrefix = "track/signalmast/"; // default for constructing topic; public for script access, set by config

    public static void setSendTopicPrefix(@Nonnull String prefix) {
        sendTopicPrefix = prefix;
        log.debug("sendTopicPrefix set to {}", prefix);
    }

    protected String makeSendTopic(String systemName) {
        String[] pieces = systemName.split("\\(");
        if (pieces.length == 2) {
            String result = pieces[1].substring(1, pieces[1].length()-1); // starts with ($)
            return sendTopicPrefix+result;
        } else {
            log.warn("not just one '(' in {}", systemName);
            return sendTopicPrefix+systemName;
        }
    }

    private static final String mastType = "IF$mqm";

    private final String sendTopic;
    private MqttAdapter mqttAdapter;

    private void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) {
            log.error("SignalMast system name needs at least three parts: {}", systemName);
            throw new IllegalArgumentException("System name needs at least three parts: " + systemName);
        }
        if (!parts[0].equals(mastType)) {
            log.warn("SignalMast system name should start with {} but is {}", mastType, systemName);
        }

        String system = parts[1];

        String mast = parts[2];
        // new style
        mast = mast.substring(0, mast.indexOf("("));
        setMastType(mast);
        String tmp = parts[2].substring(parts[2].indexOf("($") + 2, parts[2].indexOf(")"));
        try {
            log.debug("Parse {} as integer from {}?", tmp, parts[2]);
            int autoNumber = Integer.parseInt(tmp);
            synchronized (MqttSignalMast.class) {
                if (autoNumber > getLastRef()) {
                    setLastRef(autoNumber);
                }
            }
        } catch (NumberFormatException e) {
            log.debug("Auto generated SystemName {} does not have numeric form, skipping autoincrement", systemName);
        }
        configureSignalSystemDefinition(system);
        configureAspectTable(system, mast);
    }

    @Override
    public void setAspect(@Nonnull String aspect) {
        // check it's a choice
        if (!map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid aspect: {} on mast: {}", aspect, getDisplayName());
            throw new IllegalArgumentException("attempting to set invalid aspect: " + aspect + " on mast: " + getDisplayName());
        } else if (disabledAspects.contains(aspect)) {
            log.warn("attempting to set an aspect that has been disabled: {} on mast: {}", aspect, getDisplayName());
            throw new IllegalArgumentException("attempting to set an aspect that has been disabled: " + aspect + " on mast: " + getDisplayName());
        }
        log.debug("Setting aspect {}", aspect);
        super.setAspect(aspect);
        report();
    }

    @Override
    public void setHeld(boolean held) {
        log.debug("Setting held {}", held);
        super.setHeld(held);
        report();
    }

    @Override
    public void setLit(boolean lit) {
        log.debug("Setting lit {}", lit);
        super.setLit(lit);
        report();
    }

    private void report() {
        String msg = aspect+"; ";
        msg = msg+ (getLit()?"Lit; ":"Unlit; ");
        msg = msg+ (getHeld()?"Held":"Unheld");
        sendMessage(msg);
    }
    private void sendMessage(String c) {
        log.debug("publishing \"{}\" on \"{}\"", c, sendTopic);
        mqttAdapter.publish(sendTopic, c);
    }

    /**
     *
     * @param newVal for ordinal of all MqttSignalMasts in use
     */
    protected static void setLastRef(int newVal) {
        lastRef = newVal;
    }

    /**
     * @return highest ordinal of all MqttSignalMasts in use
     */
    public static int getLastRef() {
        return lastRef;
    }

    @Override
    public void notifyMqttMessage(String receivedTopic, String payload) {
        if (! ( receivedTopic.endsWith(sendTopic) ) ) {
            log.error("{} got a message whose topic ({}) wasn't for me ({})", getDisplayName(), receivedTopic, sendTopic);
            return;
        }
        
        // parse and  act
        var parts =  payload.split(";");
        int length = parts.length;
        // part 0 is aspect
        if (length >= 1) {
            // parts[0] is the aspect name
            String aspect = parts[0].trim();
            // check it's a choice
            if (!map.checkAspect(aspect)) {
                // not a valid aspect
                log.warn("received invalid aspect: {} on mast: {}", aspect, getDisplayName());
                throw new IllegalArgumentException("attempting to set invalid aspect: " + aspect + " on mast: " + getDisplayName());
            } else if (disabledAspects.contains(aspect)) {
                log.warn("received an aspect that has been disabled: {} on mast: {}", aspect, getDisplayName());
                throw new IllegalArgumentException("attempting to set an aspect that has been disabled: " + aspect + " on mast: " + getDisplayName());
            }
            log.debug("Setting aspect {} from received payload", aspect);
            super.setAspect(aspect);
            
        } else {
            log.error("{} got message with empty payload", getDisplayName());
        }
        if (length >= 2) {
            // parts[1] is the Lit status
            if (parts[1].trim().equals("Unlit")) {
                super.setLit(false); // calling this class's setLit sends report
            } else  {
                super.setLit(true); // calling this class's setLit sends report
            }
        } 
        if (length >= 3) {
            // parts[2] is the Held status
            if (parts[2].trim().equals("Held")) {
                super.setHeld(true); // calling this class's setHeld sends report
            } else  {
                super.setHeld(false); // calling this class's setHeld sends report
            }
        }
    }

    /**
     * Ordinal of all MqttSignalMasts to create unique system name.
     */
    private static volatile int lastRef = 0;

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttSignalMast.class);

}
