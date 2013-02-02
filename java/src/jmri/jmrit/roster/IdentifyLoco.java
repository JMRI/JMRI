// IdentifyLoco.java

package jmri.jmrit.roster;

import org.apache.log4j.Logger;
import jmri.*;

/**
 * Interact with a programmer to identify the RosterEntry for a loco
 * on the programming track.
 *
 * This is a class (instead of a Roster member function) to simplify use of
 * ProgListener callbacks. It is abstract as we expect that local classes
 * will define the message and done members.
 *
 * Once started, this maintains a List of possible RosterEntrys as
 * it works through the identification progress.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version     $Revision$
 * @see         jmri.jmrit.roster.RosterEntry
 */
abstract public class IdentifyLoco extends jmri.jmrit.AbstractIdentify {

    protected boolean shortAddr;
    private int cv17val;
    private int cv18val;
    protected int cv7val;
    protected int cv8val;
    int address = -1;

    int originalMode = Programmer.NONE;

    // steps of the identification state machine
    public boolean test1() {
        Programmer p = InstanceManager.programmerManagerInstance().getGlobalProgrammer();
        // if long address, we have to use some mode other
        // than register, so remember where we are now
        originalMode = p.getMode();
        // request contents of CV 29
        statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("READ CV 29"));
        readCV(29);
        return false;
    }

    public boolean test2(int value) {
        // check for long address vs short address
        if ( (value&0x20) != 0 ) {
            // long address needed
            shortAddr = false;
            // might now be in register mode, which is no good.
            // can we use original mode?
            Programmer p = InstanceManager.programmerManagerInstance().getGlobalProgrammer();
            if (originalMode==Programmer.PAGEMODE ||
                originalMode==Programmer.DIRECTBITMODE ||
                originalMode==Programmer.DIRECTBYTEMODE) {
                // yes, set to that original mode
                p.setMode(originalMode);
            } else if (p.hasMode(Programmer.DIRECTBITMODE)) {
                p.setMode(Programmer.DIRECTBITMODE);
            } else if (p.hasMode(Programmer.PAGEMODE)) {
                p.setMode(Programmer.PAGEMODE);
            } else if (p.hasMode(Programmer.DIRECTBYTEMODE)) {
                p.setMode(Programmer.DIRECTBYTEMODE);
            } else {
                // failed, as couldn't set a useful mode!
                log.error("can't set programming mode, long address fails");
                address = -1;
                statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("LONG ADDRESS - READ CV 17"));
                return true;  // Indicates done
            }
            // mode OK, continue operation
            statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("LONG ADDRESS - READ CV 17"));
            readCV(17);
        } else {
            // short - read address
            shortAddr = true;
            statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("SHORT ADDRESS - READ CV 1"));
            readCV(1);
        }
        return false;
    }

    public boolean test3(int value) {
        // check if we're reading short or long
        if (shortAddr) {
            // short - this is the address
            address = value;
            statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("READMFG"));
            readCV(7);
            return false;
        } else {
            // long - need CV18 also
            cv17val = value;
            statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("LONG ADDRESS - READ CV 18"));
            readCV(18);
            return false;
        }
    }
    
    public boolean test4(int value) {
        // only for long address
        if (shortAddr) {
            cv7val = value;
            statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("READMFGVER"));
            readCV(8);
            return false;
            
        }

        // value is CV18, calculate address
        cv18val = value;
        address = (cv17val&0x3f)*256 + cv18val;
        statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("READMFG"));
        readCV(7);
        return false;
    }

    public boolean test5(int value) {
        if(shortAddr){
            cv8val = value;
            //We have read manufacturer and decoder version details
            return true;
        }
        statusUpdate(java.util.ResourceBundle.getBundle("jmri/jmrit/roster/JmritRosterBundle").getString("READMFGVER"));
        readCV(8);
        cv7val = value;
        return false;
    }

    public boolean test6(int value) {
        if(shortAddr){
            log.error("test4 routine reached in short address mode");
            return true;
        }
        cv8val = value;
        return true;
    }

    public boolean test7(int value) {
        log.error("unexpected step 7 reached with value: "+value);
        return true;
    }

    public boolean test8(int value) {
        log.error("unexpected step 8 reached with value: "+value);
        return true;
    }

    protected void statusUpdate(String s) {
        message(s);
        if (s.equals("Done")) done(address);
        else if (log.isDebugEnabled()) log.debug("received status: "+s);
    }

    abstract protected void done(int address);

    abstract protected void message(String m);

    // initialize logging
    static Logger log = Logger.getLogger(IdentifyLoco.class.getName());

}
