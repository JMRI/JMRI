package jmri.jmrix.mqtt;

import javax.annotation.Nonnull;

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
    @Nonnull private final String systemPrefix;

    public void setPrefix(@Nonnull String namePrefix) {
        this.namePrefix = namePrefix;
    }
    private String namePrefix = "track/turnout/";
    
    public MqttTurnoutManager(@Nonnull MqttAdapter ma, @Nonnull String p) {
        super();
        mqttAdapter = ma;
        systemPrefix = p;        
    }

    @Override
    public String getSystemPrefix() {
        return systemPrefix;
    }

    @Override
    public Turnout createNewTurnout(@Nonnull String systemName, String userName) {
        MqttTurnout t;
        String suffix = systemName.substring(systemPrefix.length() + 1);
        String topic = namePrefix+suffix;
        
        // if an integer, this is a legacy name
        try { 
            int addr = Integer.parseInt(suffix);
        } catch (IllegalArgumentException e) {
            log.trace("{} is not an integer", suffix);
        }
        
        t = new MqttTurnout(mqttAdapter, topic);
        t.setUserName(userName);

        if (parser != null) t.setParser(parser);
        
        return t;
    }

    public void setParser(MqttContentParser<Turnout> parser) {
        this.parser = parser;
    }
    MqttContentParser<Turnout> parser = null;
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttTurnoutManager.class);
}

