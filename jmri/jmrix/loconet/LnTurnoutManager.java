// LnTurnoutManager.java

package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.Turnout;

/**
 * LnTurnoutManager implements the TurnoutManager.
 * <P>
 * System names are "LTnnn", where nnn is the turnout number without padding.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 * Description:		Implement turnout manager for loconet
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version         $Revision: 1.8 $
 */

public class LnTurnoutManager extends jmri.AbstractTurnoutManager implements LocoNetListener {

    final String prefix = "LT";

    /**
     * @return The system-specific prefix letter for a specific implementation
     */
    public char systemLetter() { return prefix.charAt(0); }

    // LocoNet-specific methods

    public void putBySystemName(LnTurnout t) {
        String system = prefix+t.getNumber();
        _tsys.put(system, t);
    }

    public Turnout newTurnout(String systemName, String userName) {
        // if system name is null, supply one from the number in userName
        if (systemName == null) systemName = prefix+userName;

        // return existing if there is one
        Turnout t;
        if ( (userName!=null) && ((t = getByUserName(userName)) != null)) return t;
        if ( (t = getBySystemName(systemName)) != null) return t;

        // get number from name
        if (!systemName.startsWith(prefix)) {
            log.error("Invalid system name for LocoNet turnout: "+systemName);
            return null;
        }
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new LnTurnout(addr);
        t.setUserName(userName);

        _tsys.put(systemName, t);
        if (userName!=null) _tuser.put(userName, t);
        t.addPropertyChangeListener(this);

        return t;
    }

    // ctor has to register for LocoNet events
    public LnTurnoutManager() {
        if (LnTrafficController.instance() != null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.error("No layout connection, turnout manager can't function");
    }

    // listen for turnouts, creating them as needed
    public void message(LocoNetMessage l) {
        // parse message type
        int addr;
        switch (l.getOpCode()) {
        case LnConstants.OPC_SW_REQ: {               /* page 9 of Loconet PE */
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
            addr = address(sw1, sw2);
            if (log.isDebugEnabled()) log.debug("SW_REQ received with address "+addr);
            break;
        }
        case LnConstants.OPC_SW_REP: {                /* page 9 of Loconet PE */
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
            addr = address(sw1, sw2);
            if (log.isDebugEnabled()) log.debug("SW_REP received with address "+addr);
            break;
        }
        default:  // here we didn't find an interesting command
            return;
        }
        // reach here for loconet switch command; make sure we know about this one
        String s = prefix+addr;
        if (null == getBySystemName(s)) {
            // need to store a new one
            LnTurnout t = new LnTurnout(addr);
            putBySystemName(t);
        }
    }

    private int address(int a1, int a2) {
        // the "+ 1" in the following converts to throttle-visible numbering
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnTurnoutManager.class.getName());

}


/* @(#)LnTurnoutManager.java */
