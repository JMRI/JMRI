// IdentifyLoco.java

package jmri.jmrit.roster;

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
 * @version     $Revision: 1.7 $
 * @see         jmri.jmrit.roster.RosterEntry
 */
abstract public class IdentifyLoco extends jmri.jmrit.AbstractIdentify {

    private boolean shortAddr;
    private int cv17val;
    private int cv18val;
    int address = -1;

    int originalMode = Programmer.NONE;

    // steps of the identification state machine
    public boolean test1() {
        Programmer p = InstanceManager.programmerManagerInstance().getServiceModeProgrammer();
        // if long address, we have to use some mode other
        // than register, so remember where we are now
        originalMode = p.getMode();
        // request contents of CV 29
        statusUpdate("Read CV 29");
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
            Programmer p = InstanceManager.programmerManagerInstance().getServiceModeProgrammer();
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
                statusUpdate("Long address - read CV 17");
                return true;  // Indicates done
            }
            // mode OK, continue operation
            statusUpdate("Long address - read CV 17");
            readCV(17);
        } else {
            // short - read address
            shortAddr = true;
            statusUpdate("Short address - read CV 1");
            readCV(1);
        }
        return false;
    }

    public boolean test3(int value) {
        // check if we're reading short or long
        if (shortAddr) {
            // short - this is the address & we're done
            address = value;
            return true;
        } else {
            // long - need CV18 also
            cv17val = value;
            statusUpdate("Long address - read CV 18");
            readCV(18);
            return false;
        }
    }

    public boolean test4(int value) {
        // only for long address
        if (shortAddr) log.error("test4 routine reached in short address mode");

        // value is CV18, calculate address
        cv18val = value;
        address = (cv17val&0x3f)*256 + cv18val;
        return true;
    }

    public boolean test5(int value) {
        log.error("unexpected step 5 reached with value: "+value);
        return true;
    }

    public boolean test6(int value) {
        log.error("unexpected step 6 reached with value: "+value);
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
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(IdentifyLoco.class.getName());

}
