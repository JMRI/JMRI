package jmri.jmrix.lenz.hornbyelite;

import jmri.Turnout;
import jmri.jmrix.lenz.XNetAddress;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement XNet turnout manager - Specific to Hornby Elite
 * <p>
 * System names are "XTnnn", where X is the user-configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Paul Bender Copyright (C) 2008
 */
public class EliteXNetTurnoutManager extends jmri.jmrix.lenz.XNetTurnoutManager {

    public EliteXNetTurnoutManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    // XNet-specific methods

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        // check if the output bit is available
        int bitNum = XNetAddress.getBitFromSystemName(systemName, getSystemPrefix());
        if (bitNum == -1) {
            return (null);
        }
        // create the new Turnout object
        Turnout t = new EliteXNetTurnout(getSystemPrefix(), bitNum, tc);
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * Listen for turnouts, creating them as needed
     */
    @Override
    public void message(jmri.jmrix.lenz.XNetReply l) {
        log.debug("received message: {}", l);
        if (l.isFeedbackBroadcastMessage()) {
            int numDataBytes = l.getElement(0) & 0x0f;
            for (int i = 1; i < numDataBytes; i += 2) {
                // parse message type
                int addr = l.getTurnoutMsgAddr(i);    // Acc. Address 1 on 
                // Hornby reads as 
                // XpressNet address 2
                // in the message.
                if (addr >= 0) {
                    log.debug("message has address: {}", addr);
                    // reach here for switch command; make sure we know 
                    // about this one
                    String s = getSystemNamePrefix() +(addr - 1);
                    forwardMessageToTurnout(s,l);
                    if ((addr & 0x01) == 1) {
                        // If the address we got was odd, we need to check to 
                        // see if the even address should be added as well.
                        int a2 = l.getElement(i + 1);
                        if ((a2 & 0x0c) != 0) {
                            // reach here for switch command; make sure we know 
                            // about this one
                            s = getSystemNamePrefix() + (addr);
                            forwardMessageToTurnout(s,l);
                        }
                    }
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EliteXNetTurnoutManager.class);

}
