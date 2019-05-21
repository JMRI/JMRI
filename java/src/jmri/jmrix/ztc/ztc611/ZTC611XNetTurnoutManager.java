package jmri.jmrix.ztc.ztc611;

import jmri.Turnout;
import jmri.jmrix.lenz.XNetAddress;

/**
 * Implement turnout manager - Specific to ZTC ZTC611
 * <p>
 * System names are "XTnnn", where X is the user-configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Paul Bender Copyright (C) 2008, 2017
 */
public class ZTC611XNetTurnoutManager extends jmri.jmrix.lenz.XNetTurnoutManager implements jmri.jmrix.lenz.XNetListener {

    public ZTC611XNetTurnoutManager(jmri.jmrix.lenz.XNetTrafficController controller, String prefix) {
        super(controller, prefix);
    }

    // XNet-specific methods
    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        // check if the output bit is available
        int bitNum = XNetAddress.getBitFromSystemName(systemName, prefix);
        if (bitNum == -1) {
            return (null);
        }
        Turnout t = new ZTC611XNetTurnout(prefix, bitNum, tc);
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    // private final static Logger log = LoggerFactory.getLogger(ZTC611XNetTurnoutManager.class);

}
