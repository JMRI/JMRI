package jmri.jmrix.mqtt;

import javax.annotation.Nonnull;
import jmri.Light;

/**
 * Implement LightManager for MQTT systems
 * <p>
 * System names are "prefixnnn", where prefix is the system prefix and nnn is
 * the light number without padding.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class MqttLightManager extends jmri.managers.AbstractLightManager {

    public MqttLightManager(@Nonnull MqttSystemConnectionMemo memo) {
        super(memo);
    }

    public void setSendTopicPrefix(@Nonnull String sendTopicPrefix) {
        this.sendTopicPrefix = sendTopicPrefix;
    }

    public void setRcvTopicPrefix(@Nonnull String rcvTopicPrefix) {
        this.rcvTopicPrefix  = rcvTopicPrefix;
    }
    
    @Nonnull
    public String sendTopicPrefix = "yard/light/";
    @Nonnull
    public String rcvTopicPrefix  = "yard/light/";

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public MqttSystemConnectionMemo getMemo() {
        return (MqttSystemConnectionMemo) memo;
    }

    @Override
    @Nonnull
    protected Light createNewLight(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        String suffix = systemName.substring(getSystemNamePrefix().length());

        String sendTopic = java.text.MessageFormat.format(
            sendTopicPrefix.contains("{0}") ? sendTopicPrefix : (sendTopicPrefix + "{0}"),
            suffix);
        String rcvTopic = java.text.MessageFormat.format(
            rcvTopicPrefix.contains("{0}") ? rcvTopicPrefix : (rcvTopicPrefix + "{0}"),
            suffix);

        Light t = new MqttLight(getMemo().getMqttAdapter(), systemName, userName, sendTopic, rcvTopic);
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean validSystemNameConfig(String systemName) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return "A string which will be inserted into \"" + sendTopicPrefix + "\" for transmission";
    }
}
