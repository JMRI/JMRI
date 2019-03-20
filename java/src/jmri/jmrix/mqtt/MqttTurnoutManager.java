package jmri.jmrix.mqtt;

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
    private final MqttAdapter mqttAdapter;
    private final String systemPrefix;

    public MqttTurnoutManager(MqttAdapter ma, String p) {
        super();
        mqttAdapter = ma;
        systemPrefix = p;        
    }

    @Override
    public String getSystemPrefix() {
        return systemPrefix;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        MqttTurnout t;
        int addr = Integer.parseInt(systemName.substring(systemPrefix.length() + 1));
        t = new MqttTurnout(mqttAdapter, addr);
        t.setUserName(userName);

        if (parser != null) t.setParser(parser);

        return t;
    }

    /** {@inheritDoc} */
    @Override
    public int getOutputInterval(String systemName) {
        if (mqttAdapter.getSystemConnectionMemo() != null) {
            return mqttAdapter.getSystemConnectionMemo().getOutputInterval();
        } else {
            return 250;
        }
    }

    public void setParser(MqttContentParser<Turnout> parser) {
        this.parser = parser;
    }
    MqttContentParser<Turnout> parser = null;

    static volatile MqttTurnoutManager _instance = null;

}
