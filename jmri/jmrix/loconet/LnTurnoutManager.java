// LnTurnoutManager.java

package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.Turnout;

/**
 * LnTurnoutManager implements the TurnoutManager.
 * <P>
 * System names are "LTnnn", where nnn is the turnout number without padding.
 *
 * Description:		Implement turnout manager for loconet
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version         $Revision: 1.4 $
 */

public class LnTurnoutManager extends jmri.AbstractTurnoutManager implements LocoNetListener {

    // ABC implementations

    // to free resources when no longer used
    public void dispose() throws JmriException {
    }
    
    // LocoNet-specific methods
    
    public void putBySystemName(LnTurnout t) {
        String system = "LT"+t.getNumber();
        _tsys.put(system, t);
    }
    
    public Turnout newTurnout(String systemName, String userName) {
        // if system name is null, supply one from the number in userName
        if (systemName == null) systemName = "LT"+userName;
        
        // return existing if there is one
        Turnout t;
        if ( (userName!=null) && ((t = getByUserName(userName)) != null)) return t;
        if ( (t = getBySystemName(systemName)) != null) return t;
        
        // get number from name
        if (!systemName.startsWith("LT")) {
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
        LnTrafficController.instance().addLocoNetListener(~0, this);
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
        String s = "LT"+addr;
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
