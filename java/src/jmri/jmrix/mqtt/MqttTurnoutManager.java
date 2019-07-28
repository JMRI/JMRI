package jmri.jmrix.mqtt;

import javax.annotation.Nonnull;

import jmri.JmriException;
import jmri.Turnout;

/**
 * Implement turnout manager for MQTT systems
 * <p>
 * System names are "MTnnn", where M is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Lionel Jeanson Copyright (c) 2017
 */
public class MqttTurnoutManager extends jmri.managers.AbstractTurnoutManager {
    @Nonnull private final MqttAdapter mqttAdapter;

    public MqttTurnoutManager(@Nonnull MqttAdapter ma, @Nonnull String p) {
        super();
        mqttAdapter = ma;
        systemPrefix = p;        
    }

    @Nonnull private final String systemPrefix; // for systemName
    @Override
    public String getSystemPrefix() {
        return systemPrefix;
    }

    public void setTopicPrefix(@Nonnull String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }
    @Nonnull public String topicPrefix = "track/turnout/"; // for constructing topic; public for script access

    /**
     * {@inheritDoc}
     *
     * Accepts any string.
     */
    @Override
    public String createSystemName(@Nonnull String topicSuffix, @Nonnull String prefix) throws JmriException {
        return prefix + typeLetter() + topicSuffix;
    }

    @Override
    public Turnout createNewTurnout(@Nonnull String systemName, String userName) {
        MqttTurnout t;
        String suffix = systemName.substring(systemPrefix.length() + 1);
        String topic = topicPrefix+suffix;

        t = new MqttTurnout(mqttAdapter, systemName, topic);
        t.setUserName(userName);

        if (parser != null) t.setParser(parser);

        return t;
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return "A string which will be appended to \""+mqttAdapter.baseTopic+topicPrefix+"\"";
    }

    public void setParser(MqttContentParser<Turnout> parser) {
        this.parser = parser;
    }
    MqttContentParser<Turnout> parser = null;
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttTurnoutManager.class);
}
