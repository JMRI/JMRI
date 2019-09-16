package jmri.jmrix.ztc.ztc611;

import jmri.Turnout;
import jmri.jmrix.lenz.XNetAddress;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;

/**
 * Implement turnout manager - Specific to ZTC ZTC611
 * <p>
 * System names are "XTnnn", where X is the user-configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Paul Bender Copyright (C) 2008, 2017
 */
public class ZTC611XNetTurnoutManager extends jmri.jmrix.lenz.XNetTurnoutManager {

    public ZTC611XNetTurnoutManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    // XNet-specific methods
    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        // check if the output bit is available
        int bitNum = XNetAddress.getBitFromSystemName(systemName, getSystemPrefix());
        if (bitNum == -1) {
            return (null);
        }
        Turnout t = new ZTC611XNetTurnout(getSystemPrefix(), bitNum, tc);
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    // private final static Logger log = LoggerFactory.getLogger(ZTC611XNetTurnoutManager.class);

}
