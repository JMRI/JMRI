package jmri.jmrix.mqtt;

import javax.annotation.*;

import jmri.Reporter;
import jmri.SystemConnectionMemo;

import jmri.managers.AbstractReporterManager;

/**
 * Provide a ReporterManager implementation for MQTT communications
 *
 * @author Bob Jacobsen Copyright (C) 2023
 */

public class MqttReporterManager extends AbstractReporterManager {

    public MqttReporterManager(@Nonnull SystemConnectionMemo memo ) {
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

    public void setRcvTopicPrefix(@Nonnull String rcvTopicPrefix) {
        this.rcvTopicPrefix = rcvTopicPrefix;
    }

    @Nonnull
    public String rcvTopicPrefix = "track/reporter/"; // for constructing topic; public for script access

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
    public String createSystemName(@Nonnull String topicSuffix, @Nonnull String prefix) {
        return prefix + typeLetter() + topicSuffix;
    }

    /**
     * Create an new Reporter object.
     * {@inheritDoc}
     * @return never null
     */
    @Nonnull
    @Override
    public Reporter createNewReporter(String systemName, String userName) {
        MqttReporter r;
        String suffix = systemName.substring(getSystemPrefix().length() + 1);


        String rcvTopic = java.text.MessageFormat.format(
                            rcvTopicPrefix.contains("{0}") ? rcvTopicPrefix : rcvTopicPrefix+"{0}",
                            suffix);

        r = new MqttReporter(getMemo().getMqttAdapter(), systemName, rcvTopic);
        r.setUserName(userName);

        return r;
    }

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

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttReporterManager.class);

}
