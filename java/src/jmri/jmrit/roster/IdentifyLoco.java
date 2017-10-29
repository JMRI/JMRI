package jmri.jmrit.roster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interact with a programmer to identify the
 * {@link jmri.jmrit.roster.RosterEntry} for a loco on the programming track.
 * <p>
 * This is a class (instead of a {@link jmri.jmrit.roster.Roster} member
 * function) to simplify use of ProgListener callbacks. It is abstract as we
 * expect that local classes will define the message and done members.
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2015
 * @see jmri.jmrit.symbolicprog.CombinedLocoSelPane
 * @see jmri.jmrit.symbolicprog.NewLocoSelPane
 */
abstract public class IdentifyLoco extends jmri.jmrit.AbstractIdentify {

    public IdentifyLoco(jmri.Programmer programmer) {
        super(programmer);
    }

    protected boolean shortAddr;
    private int cv17val;
    private int cv18val;
    protected int cv7val;
    protected int cv8val;
    int address = -1;

    // steps of the identification state machine
    @Override
    public boolean test1() {
        // request contents of CV 29
        statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("READ CV 29"));
        readCV("29");
        return false;
    }

    @Override
    public boolean test2(int value) {
        // check for long address vs short address
        if ((value & 0x20) != 0) {
            // long address needed
            shortAddr = false;
            statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("LONG ADDRESS - READ CV 17"));
            readCV("17");
        } else {
            // short - read address
            shortAddr = true;
            statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("SHORT ADDRESS - READ CV 1"));
            readCV("1");
        }
        return false;
    }

    @Override
    public boolean test3(int value) {
        // check if we're reading short or long
        if (shortAddr) {
            // short - this is the address
            address = value;
            statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("READMFG"));
            readCV("7");
            return false;
        } else {
            // long - need CV18 also
            cv17val = value;
            statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("LONG ADDRESS - READ CV 18"));
            readCV("18");
            return false;
        }
    }

    @Override
    public boolean test4(int value) {
        // only for long address
        if (shortAddr) {
            cv7val = value;
            statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("READMFGVER"));
            readCV("8");
            return false;

        }

        // value is CV18, calculate address
        cv18val = value;
        address = (cv17val & 0x3f) * 256 + cv18val;
        statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("READMFG"));
        readCV("7");
        return false;
    }

    @Override
    public boolean test5(int value) {
        if (shortAddr) {
            cv8val = value;
            //We have read manufacturer and decoder version details
            return true;
        }
        statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("READMFGVER"));
        readCV("8");
        cv7val = value;
        return false;
    }

    @Override
    public boolean test6(int value) {
        if (shortAddr) {
            log.error("test4 routine reached in short address mode");
            return true;
        }
        cv8val = value;
        return true;
    }

    @Override
    public boolean test7(int value) {
        log.error("unexpected step 7 reached with value: " + value);
        return true;
    }

    @Override
    public boolean test8(int value) {
        log.error("unexpected step 8 reached with value: " + value);
        return true;
    }
    @Override
    public boolean test9(int value) {
        log.error("unexpected step 9 reached with value: " + value);
        return true;
    }

    @Override
    protected void statusUpdate(String s) {
        message(s);
        if (s.equals("Done")) {
            done(address);
        } else if (log.isDebugEnabled()) {
            log.debug("received status: " + s);
        }
    }

    abstract protected void done(int address);

    abstract protected void message(String m);

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(IdentifyLoco.class);

}
