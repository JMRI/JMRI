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

    private JMRIClientSystemConnectionMemo memo = null;
    private String prefix = null;

    public JMRIClientLightManager(JMRIClientSystemConnectionMemo memo) {
        this.memo = memo;
        this.prefix = memo.getSystemPrefix();
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public Light createNewLight(String systemName, String userName) {
        Light t;
        int addr = Integer.parseInt(systemName.substring(prefix.length() + 1));
        t = new JMRIClientLight(addr, memo);
        t.setUserName(userName);
        return t;
    }

    /**
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return ((systemName.startsWith(prefix + "l")
                || systemName.startsWith(prefix + "L"))
                && Integer.parseInt(systemName.substring(prefix.length() + 1)) > 0) ? NameValidity.VALID : NameValidity.INVALID;
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
