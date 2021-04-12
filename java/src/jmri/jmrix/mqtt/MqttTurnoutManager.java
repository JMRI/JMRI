package jmri.jmrix.mqtt;

import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Turnout;

/**
 * Implement turnout manager for MQTT systems
 * <p>
 * System names are "MTnnn", where M is the user configurable system prefix, nnn
 * is the turnout number without padding.
 *
 * @author Lionel Jeanson Copyright (c) 2017
 */
public class MqttTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    /**
     *
     * @param ma the adapter for this manager
     * @param p  an ignored value
     * @deprecated since 4.17.3; use {@link #MqttTurnoutManager(jmri.jmrix.mqtt.MqttSystemConnectionMemo)} instead
     */
    @Deprecated
    public MqttTurnoutManager(@Nonnull MqttAdapter ma, @Nonnull String p) {
        this(ma.getSystemConnectionMemo());
    }

    public MqttTurnoutManager(@Nonnull MqttSystemConnectionMemo memo) {
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

    /**
     * {@inheritDoc}
     * <p>
     * Accepts any string as the "topicSuffix"
     */
    @Override
    public String createSystemName(@Nonnull String topicSuffix, @Nonnull String prefix) throws JmriException {
        return prefix + typeLetter() + topicSuffix;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        MqttTurnout t;
        String suffix = systemName.substring(getSystemNamePrefix().length());

        String sendTopic = java.text.MessageFormat.format(
                            sendTopicPrefix.contains("{0}") ? sendTopicPrefix : sendTopicPrefix+"{0}",
                            suffix);
        String rcvTopic = java.text.MessageFormat.format(
                            rcvTopicPrefix.contains("{0}") ? rcvTopicPrefix : rcvTopicPrefix+"{0}",
                            suffix);

        t = new MqttTurnout(getMemo().getMqttAdapter(), systemName, sendTopic, rcvTopic);
        t.setUserName(userName);

        if (parser != null) {
            t.setParser(parser);
        }

        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return "A string which will be inserted into \"" + sendTopicPrefix + "\" for transmission";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    public void setParser(MqttContentParser<Turnout> parser) {
        this.parser = parser;
    }
    MqttContentParser<Turnout> parser = null;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttTurnoutManager.class);
}
