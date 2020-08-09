package jmri.jmrix.mqtt;

import javax.annotation.*;

import jmri.*;
import jmri.implementation.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the MqttSensorManager interface.
 *
 * @author  Bob Jacobsen Copyright (C) 2001, 2003, 2006, 2019
 */
public class MqttSensorManager extends jmri.managers.AbstractSensorManager {

    public MqttSensorManager(@Nonnull SystemConnectionMemo memo ) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public MqttSystemConnectionMemo getMemo() {
        return (MqttSystemConnectionMemo) memo;
    }

    public void setSendTopicPrefix(@Nonnull String sendTopicPrefix) {
        this.sendTopicPrefix = sendTopicPrefix;
    }
    public void setRcvTopicPrefix(@Nonnull String rcvTopicPrefix) {
        this.rcvTopicPrefix = rcvTopicPrefix;
    }

    @Nonnull
    public String sendTopicPrefix = "track/turnout/"; // for constructing topic; public for script access
    @Nonnull
    public String rcvTopicPrefix = "track/turnout/"; // for constructing topic; public for script access
    
    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * Accepts any string.
     */
    @Override
    public String createSystemName(@Nonnull String topicSuffix, @Nonnull String prefix) throws JmriException {
        return prefix + typeLetter() + topicSuffix;
    }
    
    /**
     * Create an new sensor object
     *
     * @return new null
     */
    @Override
    protected Sensor createNewSensor(String systemName, String userName) {
        MqttSensor s;
        String suffix = systemName.substring(getSystemPrefix().length() + 1);


        String sendTopic = java.text.MessageFormat.format(
                            sendTopicPrefix.contains("{0}") ? sendTopicPrefix : sendTopicPrefix+"{0}",
                            suffix);
        String rcvTopic = java.text.MessageFormat.format(
                            rcvTopicPrefix.contains("{0}") ? rcvTopicPrefix : rcvTopicPrefix+"{0}",
                            suffix);

        s = new MqttSensor(getMemo().getMqttAdapter(), systemName, sendTopic, rcvTopic);
        s.setUserName(userName);

        if (parser != null) s.setParser(parser);

        return s;
    }

    static int defaultState = Sensor.UNKNOWN;

    public static synchronized void setDefaultStateForNewSensors(int defaultSetting) {
        log.debug("Default new-Sensor state set to {}", defaultSetting);
        defaultState = defaultSetting;
    }

    public static synchronized int getDefaultStateForNewSensors() {
        return defaultState;
    }

    protected String prefix = "M";

    /** {@inheritDoc} */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return NameValidity.VALID;
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    /** {@inheritDoc} */
    @Override
    public String getNextValidAddress(String curAddress, String prefix) {
        // If the hardware address passed does not already exist then this can
        // be considered the next valid address.
        Sensor s = getBySystemName(prefix + typeLetter() + curAddress);
        if (s == null) {
            return curAddress;
        }

        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
        try {
            iName = Integer.parseInt(curAddress);
        } catch (NumberFormatException ex) {
            log.error("Unable to convert " + curAddress + " Hardware Address to a number");
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage("Error", "Unable to convert " + curAddress + " to a valid Hardware Address", "" + ex, "", true, false);
            return null;
        }
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        s = getBySystemName(prefix + typeLetter() + iName);
        if (s != null) {
            for (int x = 1; x < 10; x++) {
                iName = iName + 1;
                s = getBySystemName(prefix + typeLetter() + iName);
                if (s == null) {
                    return Integer.toString(iName);
                }
            }
            return null;
        } else {
            return Integer.toString(iName);
        }
    }

    public void setParser(MqttContentParser<Sensor> parser) {
        this.parser = parser;
    }
    MqttContentParser<Sensor> parser = null;

    private final static Logger log = LoggerFactory.getLogger(MqttSensorManager.class);

}
