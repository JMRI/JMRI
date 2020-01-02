package jmri.jmrix.roco.z21;

import jmri.Turnout;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement z21 turnout manager.
 * <p>
 * System names are "XTnnn", where X is the user-configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author	Paul Bender Copyright (C) 2016 
 */
public class Z21XNetTurnoutManager extends XNetTurnoutManager {

    public Z21XNetTurnoutManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    // XNet-specific methods
    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        Turnout t = new Z21XNetTurnout(getSystemPrefix(), addr, tc);
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    // listen for turnouts, creating them as needed
    @Override
    public void message(XNetReply l) {
        log.debug("received message: {}", l);
        if (l.getElement(0)==Z21Constants.LAN_X_TURNOUT_INFO) {
          // bytes 2 and 3 are the address.
          int address = (l.getElement(1) << 8) + l.getElement(2);
          // the address sent byte the Z21 is one less than what JMRI's 
          // XpressNet code (and lenz systems) expect.
          address = address + 1; 
          log.debug("message has address: {}", address);
          // make sure we know about this turnout.
          String s = getSystemNamePrefix() + address;
          forwardMessageToTurnout(s,l);
        } else {
          super.message(l); // let the XpressNetTurnoutManager code
                            // handle any other replies.
        }
    }

    @Override
    protected void forwardMessageToTurnout(String s, XNetReply l){
        Z21XNetTurnout t = (Z21XNetTurnout) getBySystemName(s);
        if ( null == t ) {
           // need to create a new one, and send the message on 
           // to the newly created object.
           ((Z21XNetTurnout) provideTurnout(s)).initMessageZ21(l);
        } else {
           // The turnout exists, forward this message to the 
           // turnout
           t.message(l);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(Z21XNetTurnoutManager.class);

}
