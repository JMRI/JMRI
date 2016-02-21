// JMRIClientLightManager.java
package jmri.jmrix.jmriclient;

import jmri.Light;

/**
 * Implement light manager for JMRIClient systems
 * <P>
 * System names are "prefixnnn", where prefix is the system prefix and nnn is
 * the light number without padding.
 *
 * @author	Paul Bender Copyright (C) 2010
 * @version	$Revision$
 */
public class JMRIClientLightManager extends jmri.managers.AbstractLightManager {

    /**
     *
     */
    private static final long serialVersionUID = -6247705424672589496L;
    private JMRIClientSystemConnectionMemo memo = null;
    private String prefix = null;

    public JMRIClientLightManager(JMRIClientSystemConnectionMemo memo) {
        this.memo = memo;
        this.prefix = memo.getSystemPrefix();
    }

    public String getSystemPrefix() {
        return prefix;
    }

    public Light createNewLight(String systemName, String userName) {
        Light t;
        int addr = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
        t = new JMRIClientLight(addr, memo);
        t.setUserName(userName);
        return t;
    }

    /**
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    public boolean validSystemNameFormat(String systemName) {
        return ((systemName.startsWith(prefix + "l")
                || systemName.startsWith(prefix + "L"))
                && Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue() > 0);
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false' for now, this method always returns 'true'; it is needed for the
     * Abstract Light class
     */
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

}

/* @(#)JMRIClientLightManager.java */
