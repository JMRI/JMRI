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
 * @version         $Revision: 1.13 $
 */

public class LnTurnoutManager extends jmri.AbstractTurnoutManager implements LocoNetListener {

    // ctor has to register for LocoNet events
    public LnTurnoutManager() {
        _instance = this;
        if (LnTrafficController.instance() != null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.error("No layout connection, turnout manager can't function");
    }

    public char systemLetter() { return 'L'; }

    public void dispose() {
        if (LnTrafficController.instance() != null)
            LnTrafficController.instance().removeLocoNetListener(~0, this);
        super.dispose();
    }

    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new LnTurnout(addr);
        t.setUserName(userName);
        t.addPropertyChangeListener(this);

        return t;
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
            // Loconet spec says 0x10 of SW2 must be 1, but we observe 0
            if ( ((sw1&0xFC)==0x78) && ((sw2&0xCF)==0x07) ) return;  // turnout interrogate msg
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
        provideTurnout("LT"+addr);
    }

    private int address(int a1, int a2) {
        // the "+ 1" in the following converts to throttle-visible numbering
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1);
    }

    static public LnTurnoutManager instance() {
        if (_instance == null) _instance = new LnTurnoutManager();
        return _instance;
    }
    static LnTurnoutManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnTurnoutManager.class.getName());
}

/* @(#)LnTurnoutManager.java */
