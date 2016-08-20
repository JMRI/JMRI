package jmri.jmrix.xpa;

import jmri.Turnout;

/**
 * Implement turnout manager for Xpa+Modem connections to XPressNet Based
 * systems.
 * <P>
 * System names are "PTnnn", where nnn is the turnout number without padding.
 * <p>
 * @author	Paul Bender Copyright (C) 2004,2016
 */
public class XpaTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    private String prefix = null;
    private XpaSystemConnectionMemo memo = null;

    public XpaTurnoutManager(XpaSystemConnectionMemo m) {
         super();
         prefix = m.getSystemPrefix();
         memo = m;
    }

    public String getSystemPrefix() {
        return prefix;
    }

    // Xpa-specific methods
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
        Turnout t = new XpaTurnout(addr,memo);
        t.setUserName(userName);
        return t;
    }

    /**
     * @deprecated since 4.3.6
     */
    @Deprecated
    synchronized static public XpaTurnoutManager instance() {
        return null;
    }

}

