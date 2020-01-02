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
    public MqttSystemConnectionMemo getMemo() {
        return (MqttSystemConnectionMemo) memo;
    }

    public void setTopicPrefix(@Nonnull String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }
    @Nonnull
    public String topicPrefix = "track/turnout/"; // for constructing topic; public for script access

    /**
     * {@inheritDoc}
     * <p>
     * Accepts any string.
     */
    @Override
    public String createSystemName(@Nonnull String topicSuffix, @Nonnull String prefix) throws JmriException {
        return prefix + typeLetter() + topicSuffix;
    }

    @Override
    public Turnout createNewTurnout(@Nonnull String systemName, String userName) {
        MqttTurnout t;
        String suffix = systemName.substring(getSystemNamePrefix().length());
        String topic = topicPrefix + suffix;

        t = new MqttTurnout(getMemo().getMqttAdapter(), systemName, topic);
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
        return "A string which will be appended to \"" + getMemo().getMqttAdapter().baseTopic + topicPrefix + "\"";
    }

    public void setParser(MqttContentParser<Turnout> parser) {
        this.parser = parser;
    }
    MqttContentParser<Turnout> parser = null;

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttTurnoutManager.class);
}
