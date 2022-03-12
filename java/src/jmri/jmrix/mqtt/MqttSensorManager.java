package jmri.jmrix.mqtt;

import javax.annotation.*;

import jmri.*;
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
     * Create an new sensor object.
     * {@inheritDoc}
     * @return never null
     */
    @Nonnull
    @Override
    protected Sensor createNewSensor(String systemName, String userName) throws IllegalArgumentException {
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

    public void setParser(MqttContentParser<Sensor> parser) {
        this.parser = parser;
    }
    MqttContentParser<Sensor> parser = null;

    private final static Logger log = LoggerFactory.getLogger(MqttSensorManager.class);

}
