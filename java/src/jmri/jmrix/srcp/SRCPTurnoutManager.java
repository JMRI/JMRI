package jmri.jmrix.srcp;

import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for SRCP systems
 * <P>
 * System names are "DTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 */
public class SRCPTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    int _bus = 0;
    SRCPBusConnectionMemo _memo = null;

    @Deprecated
    public SRCPTurnoutManager() {

    }

    public SRCPTurnoutManager(SRCPBusConnectionMemo memo, int bus) {
        _bus = bus;
        _memo = memo;
    }

    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.valueOf(systemName.substring(_memo.getSystemPrefix().length() + 1)).intValue();
        t = new SRCPTurnout(addr, _memo);
        t.setUserName(userName);

        return t;
    }

    /*
     * @deprecated since 4.3.5
     */
    @Deprecated
    static public SRCPTurnoutManager instance() {
        log.error("Deprecated instance() method called");
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SRCPTurnout.class.getName());

}

/* @(#)SRCPTurnoutManager.java */
