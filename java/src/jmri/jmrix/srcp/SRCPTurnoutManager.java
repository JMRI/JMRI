package jmri.jmrix.srcp;

import jmri.Turnout;

/**
 * Implement turnout manager for SRCP systems.
 * <p>
 * System names are "DTnnn", where D is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 */
public class SRCPTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    int _bus = 0;

    public SRCPTurnoutManager(SRCPBusConnectionMemo memo, int bus) {
        super(memo);
        _bus = bus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SRCPBusConnectionMemo getMemo() {
        return (SRCPBusConnectionMemo) memo;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        t = new SRCPTurnout(addr, getMemo());
        t.setUserName(userName);

        return t;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    // private final static Logger log = LoggerFactory.getLogger(SRCPTurnout.class);

}
