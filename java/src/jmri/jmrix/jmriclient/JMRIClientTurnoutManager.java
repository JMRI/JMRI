package jmri.jmrix.jmriclient;

import jmri.Turnout;

/**
 * Implement turnout manager for JMRIClient systems
 * <p>
 * System names are "prefixnnn", where prefix is the system prefix and nnn is
 * the turnout number without padding.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class JMRIClientTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    private JMRIClientSystemConnectionMemo memo = null;
    private String prefix = null;

    public JMRIClientTurnoutManager(JMRIClientSystemConnectionMemo memo) {
        this.memo = memo;
        this.prefix = memo.getSystemPrefix();
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.parseInt(systemName.substring(prefix.length() + 1));
        t = new JMRIClientTurnout(addr, memo);
        t.setUserName(userName);
        return t;
    }

    /*
     * JMRIClient Turnouts can take arbitrary names to match the names used
     * on the server.
     */
    @Override
    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException {
        return prefix + typeLetter() + curAddress;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

}
