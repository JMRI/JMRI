/**
 * XNetTurnout.java
 *
 * Description:		extend jmri.AbstractTurnout for XNet layouts
 * @author			Bob Jacobsen Copyright (C) 2001, Portions by Paul Bender Copyright (C) 2003 
 * @version			$Revision: 1.17 $
 */

package jmri.jmrix.lenz;

import jmri.AbstractTurnout;

public class XNetTurnout extends AbstractTurnout implements XNetListener {

    public XNetTurnout(int pNumber) {  // a human-readable turnout number must be specified!
        super("XT"+pNumber);
        mNumber = pNumber;
        // At construction, register for messages
        XNetTrafficController.instance().addXNetListener(~0, this);
    }

    public int getNumber() { return mNumber; }

    // Set the Commanded State.   This method overides setCommandedState in 
    // the Abstract Turnout class.
    public void setCommandedState(int s){
	forwardCommandChangeToLayout(s);
	newCommandedState(s);
	if( getKnownState()==UNKNOWN) newKnownState(s);
    }


    // Handle a request to change state by sending an XPressNet command
    protected void forwardCommandChangeToLayout(int s) {
        // find the command station
        LenzCommandStation cs = XNetTrafficController.instance().getCommandStation();
        // get the right packet
        XNetMessage msg = cs.getTurnoutCommandMsg(mNumber,
                                                  (s & CLOSED)!=0,
                                                  (s & THROWN)!=0,
                                                  true );

        XNetTrafficController.instance().sendXNetMessage(msg, this);
    }


    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //										Object oldValue,
    //										Object newValue)
    // _once_ if anything has changed state (or set the commanded state directly)
    public void message(XNetMessage l) {
	// We have three cases to check if CommandedState does 
        // not equal KnownState, otherwise, we only want to check to 
	// see if the messages we recieve indicate this turnout chagned 
        // state
        if(getCommandedState()==getKnownState()) {
	   if(XNetTrafficController.instance().getCommandStation()
                                              .isFeedbackMessage(l)) {
           // This is a feedback message, we need to check and see if it
           // indicates this turnout is to change state or if it is for 
           // another turnout.
	    parseFeedbackMessage(l);
           }
	} else if(XNetTrafficController.instance().getCommandStation()
                                                  .isFeedbackMessage(l)) {
          int messageType= XNetTrafficController.instance()
                                                .getCommandStation()
                                                .getFeedbackMessageType(l);
	  if(messageType == 1) {
	     // The first case is that we recieve a message for this turnout
             // and this turnout provides feedback.
             // In this case, we want to check to see if the turnout has 
             // completed it's movement before doing anything else.
	     if(!motionComplete(l)) {
             // If the motion is NOT complete, send a feedback request for
             // this nibble
             XNetMessage msg =  XNetTrafficController.instance()
                                                .getCommandStation()
	   					.getFeedbackRequestMsg(mNumber,
                                                            ((mNumber%4)<=1));
             XNetTrafficController.instance().sendXNetMessage(msg, this);
             } else {
               // If the motion is completed, behave as though this is a 
               // turnout without feedback.
	       parseFeedbackMessage(l);
               // We need to tell the turnout to shut off the output.
               XNetMessage msg =  XNetTrafficController.instance()
                                                  .getCommandStation()
	   					  .getTurnoutCommandMsg(mNumber,
                                                  getCommandedState()==CLOSED,
                                                  getCommandedState()==THROWN,
                                                  false );
 	       // We have to send this message twice for some reason, 
               // otherwise, the turnout continues to throw.
               XNetTrafficController.instance().sendXNetMessage(msg, this);
               XNetTrafficController.instance().sendXNetMessage(msg, this);
             }       
          } else if (messageType == 0) {
          // The second case is that we recieve a message about this 
          // turnout, and this turnout does not provide feedback.
          // In this case, we want to check the contents of the message 
          // and act accordingly.
	    parseFeedbackMessage(l);
            // We need to tell the turnout to shut off the output.
            XNetMessage msg =  XNetTrafficController.instance()
                                                .getCommandStation()
	   					  .getTurnoutCommandMsg(mNumber,
                                                  getCommandedState()==CLOSED,
                                                  getCommandedState()==THROWN,
                                                  false );
 	    // We have to send this message twice for some reason, 
            // otherwise, the turnout continues to throw.
            XNetTrafficController.instance().sendXNetMessage(msg, this);
            XNetTrafficController.instance().sendXNetMessage(msg, this);
            }
	} else if (XNetTrafficController.instance()
                                        .getCommandStation().isOkMessage(l)) {
            // Finally, we may just recieve an OK message.
            // We need to tell the turnout to shut off the output.
            XNetMessage msg =  XNetTrafficController.instance()
                                                .getCommandStation()
	   					.getTurnoutCommandMsg(mNumber,
                                                  getCommandedState()==CLOSED,
                                                  getCommandedState()==THROWN,
                                                  false );
 	    // We have to send this message twice for some reason, 
            // otherwise, the turnout continues to throw.
            XNetTrafficController.instance().sendXNetMessage(msg, this);
            XNetTrafficController.instance().sendXNetMessage(msg, this);
	    // Set the known state to the commanded state.
	    newKnownState(getCommandedState());
	} else { return; }
    }

     /*
      * parse the feedback message, and set the status of the turnout 
      * accordingly
      */
     private int parseFeedbackMessage(XNetMessage l) {
        // check validity & addressing
        // if this is an ODD numbered turnout, then we always get the 
        // right response from .getTurnoutMsgAddr.  If this is an even 
        // numbered turnout, we need to check the messages for the odd 
        // numbered turnout in the nibble as well.
        if (mNumber%2==1 && (XNetTrafficController.instance()
            .getCommandStation()
            .getTurnoutMsgAddr(l) == mNumber)) {
            // is for this object, parse the message
            if (log.isDebugEnabled()) log.debug("Message for turnout" + mNumber);
            if(XNetTrafficController.instance()
                                    .getCommandStation()
                                    .getTurnoutStatus(l,1)==THROWN) {
               newCommandedState(THROWN);
               newKnownState(getCommandedState());
            } else if(XNetTrafficController.instance()
                                    .getCommandStation()
                                    .getTurnoutStatus(l,1)==CLOSED) { 
               newCommandedState(CLOSED);
               newKnownState(getCommandedState());
            } else return -1;
        } else if (((mNumber%2)==0) && 
                   (XNetTrafficController.instance()
                                 .getCommandStation()
                                 .getTurnoutMsgAddr(l) == mNumber-1)) {
            // is for this object, parse message type
            if (log.isDebugEnabled()) log.debug("Message for turnout" + mNumber);
            if(XNetTrafficController.instance()
                                    .getCommandStation()
                                    .getTurnoutStatus(l,0)==THROWN) {
               newCommandedState(THROWN);
               newKnownState(getCommandedState());
            } else if(XNetTrafficController.instance()
                                    .getCommandStation()
                                    .getTurnoutStatus(l,0)==CLOSED) { 
               newCommandedState(CLOSED);
               newKnownState(getCommandedState());
            } else return -1;
        }    
       return(-1);
    }

     /*
      * Determine if this feedback message says the turnout has completed 
      * it's motion or not.  Returns true for mostion complete, false 
      * otherwise. 
      */
     private boolean motionComplete(XNetMessage l) {
        // check validity & addressing
        // if this is an ODD numbered turnout, then we always get the 
        // right response from .getTurnoutMsgAddr.  If this is an even 
        // numbered turnout, we need to check the messages for the odd 
        // numbered turnout in the nibble as well.
        if (mNumber%2==1 && (XNetTrafficController.instance()
            .getCommandStation()
            .getTurnoutMsgAddr(l) == mNumber)) {
            // is for this object, parse the message
            int messageType= XNetTrafficController.instance()
                                                .getCommandStation()
                                                .getFeedbackMessageType(l);
	  if(messageType == 1) {
             int a2=l.getElement(2);
             if((a2 & 0x80)==0x80) { return false;
             } else { return true; }
	  } else return false;
        } else if (((mNumber%2)==0) && 
                   (XNetTrafficController.instance()
                                 .getCommandStation()
                                 .getTurnoutMsgAddr(l) == mNumber-1)) {
            // is for this object, parse the message
          int messageType= XNetTrafficController.instance()
                                                .getCommandStation()
                                                .getFeedbackMessageType(l);
	  if(messageType == 1) {
             int a2=l.getElement(2);
             if((a2&0x80)==0x80) { return false;
             } else { return true; }
	  } else return false;            
        }    
       return(false);
    }

    public void dispose() {
        XNetTrafficController.instance().removeXNetListener(~0, this);
    }

    // data members
    int mNumber;   // loconet turnout number

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTurnout.class.getName());

}


/* @(#)XNetTurnout.java */
