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
public class MqttSignalMast extends AbstractSignalMast {

    public MqttSignalMast(String systemName, String userName) {
        super(systemName, userName);
        configureFromName(systemName);
        sendTopic = sendTopicPrefix+systemName;
        mqttAdapter = jmri.InstanceManager.getDefault(MqttSystemConnectionMemo.class).getMqttAdapter();
    }

    public MqttSignalMast(String systemName) {
        super(systemName);
        configureFromName(systemName);
        sendTopic = sendTopicPrefix+systemName;
        mqttAdapter = jmri.InstanceManager.getDefault(MqttSystemConnectionMemo.class).getMqttAdapter();
    }

    @Nonnull
    public static String sendTopicPrefix = "track/signalmast/"; // for constructing topic; public for script access
    public static void setSendTopicPrefix(@Nonnull String prefix) {
        sendTopicPrefix = prefix;
        log.info("sendTopicPrefix set to {}", prefix);
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
            int autoNumber = Integer.parseInt(tmp);
            synchronized (MqttSignalMast.class) {
                if (autoNumber > getLastRef()) {
                    setLastRef(autoNumber);
                }
            }
        } catch (NumberFormatException e) {
            log.warn("Auto generated SystemName {} is not in the correct format", systemName);
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
        log.info("Setting aspect {}", aspect);
        super.setAspect(aspect);
        report();
    }

    @Override
    public void setHeld(boolean held) {
        log.info("Setting held {}", held);
        super.setHeld(held);
        report();
    }

    @Override
    public void setLit(boolean lit) {
        log.info("Setting lit {}", lit);
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
        log.info("publishing \"{}\" on \"{}\"", c, sendTopic);
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

    /**
     * Ordinal of all MqttSignalMasts to create unique system name.
     */
    private static volatile int lastRef = 0;

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttSignalMast.class);

}
