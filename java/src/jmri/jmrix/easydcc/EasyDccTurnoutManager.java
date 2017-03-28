package jmri.jmrix.easydcc;

import jmri.Turnout;

/**
 * Implement turnout manager for EasyDcc systems
 * <P>
 * System names are "ETnnn", where nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001
  */
public class EasyDccTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public EasyDccTurnoutManager() {

    }

    @Override
    public String getSystemPrefix() {
        return "E";
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new EasyDccTurnout(addr);
        t.setUserName(userName);

        return t;
    }

    static public EasyDccTurnoutManager instance() {
        if (_instance == null) {
            _instance = new EasyDccTurnoutManager();
        }
        return _instance;
    }
    static EasyDccTurnoutManager _instance = null;

}


