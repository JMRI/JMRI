// XNetTurnoutManager.java

package jmri.jmrix.lenz;

import jmri.Turnout;

/**
 * Implement turnout manager.
 * <P>
 * System names are "XTnnn", where nnn is the turnout number without padding.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.9 $
 */
public class XNetTurnoutManager extends jmri.AbstractTurnoutManager implements XNetListener {

    // ctor has to register for XNet events
    public XNetTurnoutManager() {
        _instance = this;
        XNetTrafficController.instance().addXNetListener(~0, this);
    }

    public char systemLetter() { return 'X'; }

    // XNet-specific methods

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        Turnout t = new XNetTurnout(addr);
        t.setUserName(userName);
        return t;
    }

    // listen for turnouts, creating them as needed
    public void message(XNetMessage l) {
        // parse message type
        int addr = XNetTrafficController.instance()
            .getCommandStation().getTurnoutMsgAddr(l);
        if (log.isDebugEnabled()) log.debug("message had address: "+addr);
        if (addr<=0)  return; // indicates no message
        // reach here for switch command; make sure we know about this one
        String s = "XT"+addr;
        if (null == getBySystemName(s)) {
            // need to create a new one
            provideTurnout(s);
        }
    }

    static public XNetTurnoutManager instance() {
        if (_instance == null) _instance = new XNetTurnoutManager();
        return _instance;
    }
    static XNetTurnoutManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTurnoutManager.class.getName());

}

/* @(#)XNetTurnoutManager.java */
