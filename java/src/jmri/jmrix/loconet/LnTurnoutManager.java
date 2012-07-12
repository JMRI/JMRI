// LnTurnoutManager.java

package jmri.jmrix.loconet;

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
 * Since LocoNet messages requesting turnout operations can arrive
 * faster than the command station can send them on the rails, 
 * the command station has a short queue of messages. When that gets full,
 * it sends a LACK, indicating that the request was not forwarded on the rails.
 * In that case, this class goes into a tight loop, resending the last 
 * turnout message seen until it's received without a LACK reply.  Note
 * two things about this:
 * <UL>
 * <LI>We provide this service for any turnout request, whether or
 * not it came from JMRI.  (This might be a problem if more than one
 * computer is executing this algorithm)
 * <LI>By sending the message as fast as we can, we tie up the LocoNet during the
 * the recovery.  This is a mixed bag; delaying can cause messages to get out
 * of sequence on the rails.  But not delaying takes up a lot of LocoNet 
 * bandwidth.
 * </UL>
 * In the end, this implementation is OK, but not great.  An improvement would
 * be to control JMRI turnout operations centrally, so that retransmissions can
 * controlled.
 * <P>
 * Description:		Implement turnout manager for loconet
 * @author			Bob Jacobsen Copyright (C) 2001, 2007
 * @version         $Revision$
 */

public class LnTurnoutManager extends jmri.managers.AbstractTurnoutManager implements LocoNetListener {

    // ctor has to register for LocoNet events
    public LnTurnoutManager(LocoNetInterface fastcontroller, LocoNetInterface throttledcontroller, String prefix, boolean mTurnoutNoRetry) {
        this.fastcontroller = fastcontroller;
        this.throttledcontroller = throttledcontroller;
        this.prefix = prefix;
        this.mTurnoutNoRetry = mTurnoutNoRetry;
        
        if (fastcontroller != null)
            fastcontroller.addLocoNetListener(~0, this);
        else
            log.error("No layout connection, turnout manager can't function");
    }

    LocoNetInterface fastcontroller;
    LocoNetInterface throttledcontroller;
    boolean mTurnoutNoRetry;
    
    String prefix;
    
    public String getSystemPrefix() { return prefix; }

    public void dispose() {
        if (fastcontroller != null)
            fastcontroller.removeLocoNetListener(~0, this);
        super.dispose();
    }

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr;
        try {
            addr = Integer.valueOf(systemName.substring(getSystemPrefix().length()+1)).intValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Can't convert "+systemName.substring(getSystemPrefix().length()+1)+" to LocoNet turnout address");
        }
        Turnout t = new LnTurnout(getSystemPrefix(), addr, throttledcontroller);
        t.setUserName(userName);
        return t;
    }

    // holds last seen turnout request for possible resend
    LocoNetMessage lastSWREQ = null;
    
    // listen for turnouts, creating them as needed
    public void message(LocoNetMessage l) {
        // parse message type
        int addr;
        switch (l.getOpCode()) {
        case LnConstants.OPC_SW_REQ: {               /* page 9 of Loconet PE */
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
            addr = address(sw1, sw2);

            // store message in case resend is needed
            lastSWREQ = l;
            
            // Loconet spec says 0x10 of SW2 must be 1, but we observe 0
            if ( ((sw1&0xFC)==0x78) && ((sw2&0xCF)==0x07) ) return;  // turnout interrogate msg
            if (log.isDebugEnabled()) log.debug("SW_REQ received with address "+addr);
            break;
        }
        case LnConstants.OPC_SW_REP: {                /* page 9 of Loconet PE */
            // clear resend message, indicating not to resend
            lastSWREQ = null;
            
            // process this request
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
            addr = address(sw1, sw2);
            if (log.isDebugEnabled()) log.debug("SW_REP received with address "+addr);
            break;
        }
        case LnConstants.OPC_LONG_ACK: { 
            // might have to resend, check 2nd byte
            if (lastSWREQ!=null && l.getElement(1)==0x30 && l.getElement(2)==0 & !mTurnoutNoRetry) {
                // received LONG_ACK reject msg, resend
                fastcontroller.sendLocoNetMessage(lastSWREQ);
            }
            
            // clear so can't resend recursively (we'll see
            // the resent message echo'd back)
            lastSWREQ = null;
            return;
        }
        default:  // here we didn't find an interesting command
            // clear resend message, indicating not to resend
            lastSWREQ = null;
            return;
        }
        // reach here for loconet switch command; make sure we know about this one
        String s = prefix+"T"+addr;
        if (getBySystemName(s) == null) {
			// no turnout with this address, is there a light
			String sx = "LL"+addr;
			if (jmri.InstanceManager.lightManagerInstance().getBySystemName(sx) == null) {
				// no light, create a turnout
				LnTurnout t = (LnTurnout) provideTurnout(s);
				t.message(l);
			}
	    }
    }

    private int address(int a1, int a2) {
        // the "+ 1" in the following converts to throttle-visible numbering
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LnTurnoutManager.class.getName());
}

/* @(#)LnTurnoutManager.java */
