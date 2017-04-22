package jmri.jmrix.nce;

import jmri.Turnout;

/**
 * Implement turnout manager for NCE systems.
 * <P>
 * System names are "NTnnn", where nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001
  */
public class NceTurnoutManager extends jmri.managers.AbstractTurnoutManager implements NceListener {

    public NceTurnoutManager(NceTrafficController tc, String prefix) {
        super();
        this.prefix = prefix;
        this.tc = tc;
    }

    String prefix = "";
    NceTrafficController tc = null;

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(getSystemPrefix().length() + 1)).intValue();
        Turnout t = new NceTurnout(tc, getSystemPrefix(), addr);
        t.setUserName(userName);

        return t;
    }

    @Override
    public void reply(NceReply r) {

    }

    @Override
    public void message(NceMessage m) {

    }
}


