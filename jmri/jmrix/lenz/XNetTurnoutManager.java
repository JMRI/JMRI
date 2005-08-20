// XNetTurnoutManager.java

package jmri.jmrix.lenz;

import jmri.Turnout;

/**
 * Implement turnout manager.
 * <P>
 * System names are "XTnnn", where nnn is the turnout number without padding.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 2.3 $
 */
public class XNetTurnoutManager extends jmri.AbstractTurnoutManager implements XNetListener {

    // ctor has to register for XNet events
    public XNetTurnoutManager() {
        _instance = this;
        XNetTrafficController.instance().addXNetListener(XNetInterface.FEEDBACK, this);
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
    public void message(XNetReply l) {
	if(log.isDebugEnabled()) log.debug("recieved message: " +l);
	if(l.isFeedbackBroadcastMessage()) {
	   int numDataBytes = l.getElement(0) & 0x0f;
	   for(int i=1;i<numDataBytes;i+=2) {
		// parse message type
        	int addr = l.getTurnoutMsgAddr(i);
        	if (addr>=0) {
        	   if (log.isDebugEnabled()) 
			log.debug("message has address: "+addr);
        	   // reach here for switch command; make sure we know 
                   // about this one
        	   String s = "XT"+addr;
                   if (null == getBySystemName(s)) {
                      // need to create a new one
                      provideTurnout(s);
                   }
                   if (addr%2==1) {
                   // If the address we got was odd, we need to check to 
                   // see if the even address should be added as well.
                   int a2=l.getElement(i+1);
                   if((a2 & 0x0c)!=0) {
                      // reach here for switch command; make sure we know 
                      // about this one
                      s = "XT"+(addr+1);
                      if (null == getBySystemName(s)) {
                         // need to create a new one
                         provideTurnout(s);
                      }
                   }
	        }
	     }
          }
       }
    }

    // listen for the messages to the LI100/LI101
    public void message(XNetMessage l) {
    }


    static public XNetTurnoutManager instance() {
        if (_instance == null) _instance = new XNetTurnoutManager();
        return _instance;
    }
    static XNetTurnoutManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTurnoutManager.class.getName());

}

/* @(#)XNetTurnoutManager.java */
