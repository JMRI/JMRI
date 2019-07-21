package jmri.jmrix.dcc;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Implement turnout manager for DCC-only systems.
 * <p>
 * System names are "BTnnn", where B is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
public class DccTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public DccTurnoutManager() {
        super(new InternalSystemConnectionMemo("B", "DCC"));
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.parseInt(systemName.substring(2)); // fixed length prefix, so (2) is OK here
        t = new DccTurnout(addr);
        t.setUserName(userName);

        return t;
    }

}
