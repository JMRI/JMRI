// EliteXNetTurnoutManager.java
package jmri.jmrix.lenz.hornbyelite;

import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager - Specific to Hornby Elite
 * <P>
 * System names are "XTnnn", where nnn is the turnout number without padding.
 *
 * @author	Paul Bender Copyright (C) 2008
 * @version	$Revision$
 */
public class EliteXNetTurnoutManager extends jmri.jmrix.lenz.XNetTurnoutManager implements jmri.jmrix.lenz.XNetListener {

    final java.util.ResourceBundle rbt = java.util.ResourceBundle.getBundle("jmri.jmrix.lenz.XNetBundle");

    public EliteXNetTurnoutManager(jmri.jmrix.lenz.XNetTrafficController controller, String prefix) {
        super(controller, prefix);
    }

    // XNet-specific methods
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        Turnout t = new EliteXNetTurnout(prefix, addr, tc);
        t.setUserName(userName);
        return t;
    }

    // listen for turnouts, creating them as needed
    public void message(jmri.jmrix.lenz.XNetReply l) {
        if (log.isDebugEnabled()) {
            log.debug("recieved message: " + l);
        }
        if (l.isFeedbackBroadcastMessage()) {
            int numDataBytes = l.getElement(0) & 0x0f;
            for (int i = 1; i < numDataBytes; i += 2) {
                // parse message type
                int addr = l.getTurnoutMsgAddr(i);    // Acc. Address 1 on 
                // Hornby reads as 
                // XPressNet address 2
                // in the message.
                if (addr >= 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("message has address: " + addr);
                    }
                    // reach here for switch command; make sure we know 
                    // about this one
                    String s = "XT" + (addr - 1);
                    if (null == getBySystemName(s)) {
                        // need to create a new one, and send the message on 
                        // to the newly created object.
                        ((EliteXNetTurnout) provideTurnout(s)).message(l);
                    } else {
                        // The turnout exists, forward this message to the 
                        // turnout
                        ((EliteXNetTurnout) getBySystemName(s)).message(l);
                    }
                    if ((addr & 0x01) == 1) {
                        // If the address we got was odd, we need to check to 
                        // see if the even address should be added as well.
                        int a2 = l.getElement(i + 1);
                        if ((a2 & 0x0c) != 0) {
                            // reach here for switch command; make sure we know 
                            // about this one
                            s = "XT" + (addr);
                            if (null == getBySystemName(s)) {
                                // need to create a new one, and send the message on 
                                // to the newly created object.
                                ((EliteXNetTurnout) provideTurnout(s)).message(l);
                            } else {
                                // The turnout exists, forward this message to the 
                                // turnout
                                ((EliteXNetTurnout) getBySystemName(s)).message(l);
                            }
                        }
                    }
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EliteXNetTurnoutManager.class.getName());

}

/* @(#)EliteXNetTurnoutManager.java */
