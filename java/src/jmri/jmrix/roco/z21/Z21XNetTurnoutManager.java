package jmri.jmrix.roco.z21;

import jmri.Turnout;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.jmrix.lenz.XNetTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager.
 * <p>
 * System names are "XTnnn", where nnn is the turnout number without padding.
 *
 * @author	Paul Bender Copyright (C) 2016 
 */
public class Z21XNetTurnoutManager extends XNetTurnoutManager implements XNetListener {

    public Z21XNetTurnoutManager(XNetTrafficController controller, String prefix) {
        super(controller,prefix);
    }

    // XNet-specific methods
    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
        Turnout t = new Z21XNetTurnout(prefix, addr, tc);
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
        if (log.isDebugEnabled()) {
            log.debug("received message: " + l);
        }
        if (l.getElement(0)==Z21Constants.LAN_X_TURNOUT_INFO) {
          // bytes 2 and 3 are the address.
          int address = (l.getElement(1) << 8) + l.getElement(2);
          // the address sent byte the Z21 is one less than what JMRI's 
          // XpressNet code (and lenz systems) expect.
          address = address + 1; 
          if(log.isDebugEnabled()) {
               log.debug("message has address: {}",address);
          }
          // make sure we know about this turnout.
          String s = prefix + typeLetter() + address;
          forwardMessageToTurnout(s,l);
        } else {
          super.message(l); // let the XpressNetTurnoutManager code
                            // handle any other replies.
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Z21XNetTurnoutManager.class);

}
