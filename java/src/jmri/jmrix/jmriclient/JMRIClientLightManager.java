package jmri.jmrix.jmriclient;

import jmri.Light;

/**
 * Implement LightManager for JMRIClient systems
 * <p>
 * System names are "prefixnnn", where prefix is the system prefix and nnn is
 * the light number without padding.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class JMRIClientLightManager extends jmri.managers.AbstractLightManager {

    public JMRIClientLightManager(JMRIClientSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JMRIClientSystemConnectionMemo getMemo() {
        return (JMRIClientSystemConnectionMemo) memo;
    }

    @Override
    public Light createNewLight(String systemName, String userName) {
        Light t;
        int addr = Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
        t = new JMRIClientLight(addr, getMemo());
        t.setUserName(userName);
        return t;
    }

    /**
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return ((systemName.startsWith(getSystemPrefix() + "l")
                || systemName.startsWith(getSystemPrefix() + "L"))
                && Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1)) > 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false' for now, this method always returns 'true'; it is needed for the
     * Abstract Light class
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

}
