/**
 * XNetTurnout.java
 *
 * Description:		extend jmri.AbstractTurnout for XNet layouts
 * <P>
 * "MONITORING" feedback modes is the only method truly supported directly by 
 * this class, though the functionality that implements these features is 
 * I dentified as being "DIRECT" mode.
 * <P>
 * As implemented for XPressNet based layouts, "DIRECT" mode waits for
 * an "OK" message to be sent in response to the turnout command just sent.
 * This mode is supported by ALL XPressNet based systems, as the "OK" is 
 * sent by the LI10x interface in response to a command for which the 
 * command station does not reply.
 * <P>
 * "MONITORING" mode is an extention of "DIRECT" mode.  This mode is not
 * supported by all XPressNet Compatible command stations. In this case,
 * the command station responds with a feedback status response indicating
 * the feedback device is an accessory decoder (and not a sensor).  In 
 * addition, the feedback status message indicates if the accessory decoder 
 * has feedback or does not have feedback
 * <P>
 * In the case of an accessory decoder without feedback, the status of the 
 * turnout is updated to reflect the status sent by the command station.  
 * <P>
 * In the  case of an accessory decoder with feedback (an LS100 for 
 * example), the status message also indicates if the motion of the 
 * accessory decoder is complete (see decoder documentation for more 
 * information).  While the message indicates the motion is not complete, 
 * we poll for an indication the motion is complete.
 * <P>
 * In either of the above cases, if the command station believes the 
 * requested motion matches the current state of the accessory decoder, 
 * the command station simply returns an "OK" message, and the behavior 
 * defaults to the basic "DIRECT" mode.
 * <P> 
 * @author			Bob Jacobsen Copyright (C) 2001, Portions by Paul Bender Copyright (C) 2003 
 * @version			$Revision: 2.4 $
 */

package jmri.jmrix.lenz;

import jmri.AbstractTurnout;

public class XNetTurnout extends AbstractTurnout implements XNetListener {

    static final int OFFSENT = 1;
    static final int IDLE = 0;
    private int InternalState = IDLE;

    public XNetTurnout(int pNumber) {  // a human-readable turnout number must be specified!
        super("XT"+pNumber);
        mNumber = pNumber;
        // At construction, register for messages
        XNetTrafficController.instance().addXNetListener(~0, this);
	// And to get property change information from the superclass
	_stateListener=new XNetTurnoutStateListener(this);
	this.addPropertyChangeListener(_stateListener);
    }

    public int getNumber() { return mNumber; }

    // Set the Commanded State.   This method overides setCommandedState in 
    // the Abstract Turnout class.
    public void setCommandedState(int s){
	forwardCommandChangeToLayout(s);
	newCommandedState(s);
	newKnownState(INCONSISTENT);
    }   

    // Handle a request to change state by sending an XPressNet command
    protected void forwardCommandChangeToLayout(int s) {
        // find the command station
        LenzCommandStation cs = XNetTrafficController.instance().getCommandStation();
        // get the right packet
        XNetMessage msg = XNetMessage.getTurnoutCommandMsg(mNumber,
                                                  (s & CLOSED)!=0,
                                                  (s & THROWN)!=0,
                                                  true );

        XNetTrafficController.instance().sendXNetMessage(msg, this);
    }

    /*
     *  Handle an incoming message from the XPressNet
     */
    public void message(XNetReply l) {
        if(InternalState==OFFSENT) {
	  // If an OFF was sent, we want to check for Communications 
          // errors before we try to do anything else. 
	  if(l.isCommErrorMessage()) {
            /* this is a communications error */
            log.error("Communications error occured - message recieved was: " + l);
	    sendOffMessage();
            return;
	  } else  if(l.isCSBusyMessage()) {
            /* this is a communications error */
            log.error("Command station busy - message recieved was: " + l);
	    sendOffMessage();
            return;
	  } else {
		InternalState=IDLE;
	        newKnownState(getCommandedState());
                return;
          }
        }

	if(getFeedbackMode()==DIRECT) {
	   // We have three cases to check if CommandedState does 
           // not equal KnownState, otherwise, we only want to check to 
	   // see if the messages we recieve indicate this turnout chagned 
           // state
           if(getCommandedState()==getKnownState()) {
	      if(l.isFeedbackMessage()) {
                 // This is a feedback message, we need to check and see if it
                 // indicates this turnout is to change state or if it is for 
                 // another turnout.
	         parseFeedbackMessage(l);
              }
	   } else if(l.isFeedbackMessage()) {
              int messageType= l.getFeedbackMessageType();
	      if(messageType == 1) {
	         // The first case is that we recieve a message for this turnout
                 // and this turnout provides feedback.
                 // In this case, we want to check to see if the turnout has 
                 // completed it's movement before doing anything else.
	         if(!motionComplete(l)) {
                    // If the motion is NOT complete, send a feedback request
		    // for this nibble
                    XNetMessage msg =  XNetMessage.getFeedbackRequestMsg(mNumber,
                                                            ((mNumber%4)<=1));
                    XNetTrafficController.instance().sendXNetMessage(msg, this);
                 } else {
                    // If the motion is completed, behave as though this is a 
                    // turnout without feedback.
	            parseFeedbackMessage(l);
                    // We need to tell the turnout to shut off the output.
	            sendOffMessage();
                 }       
              } else if (messageType == 0) {
                  // The second case is that we recieve a message about this 
                  // turnout, and this turnout does not provide feedback.
                  // In this case, we want to check the contents of the 
                  // message and act accordingly.
	          parseFeedbackMessage(l);
                  // We need to tell the turnout to shut off the output.
	          sendOffMessage();
              }
	   } else if (l.isOkMessage()) {
              // Finally, we may just recieve an OK message.
	      sendOffMessage();
	   } else return;
        }
    }

    // listen for the messages to the LI100/LI101
    public void message(XNetMessage l) {
    }


    /* Send an "Off" message to the decoder for this output  */
    private void sendOffMessage() {
            // We need to tell the turnout to shut off the output.
            XNetMessage msg =  XNetMessage.getTurnoutCommandMsg(mNumber,
                                                  getCommandedState()==CLOSED,
                                                  getCommandedState()==THROWN,
                                                  false );
            XNetTrafficController.instance().sendXNetMessage(msg, this);
	    // Set the known state to the commanded state.
	    newKnownState(getCommandedState());
	    InternalState = OFFSENT;
    }




     /*
      * parse the feedback message, and set the status of the turnout 
      * accordingly
      */
     private int parseFeedbackMessage(XNetReply l) {
        // check validity & addressing
        // if this is an ODD numbered turnout, then we always get the 
        // right response from .getTurnoutMsgAddr.  If this is an even 
        // numbered turnout, we need to check the messages for the odd 
        // numbered turnout in the nibble as well.
        if (mNumber%2==1 && (l.getTurnoutMsgAddr() == mNumber)) {
            // is for this object, parse the message
            if (log.isDebugEnabled()) log.debug("Message for turnout" + mNumber);
            if(l.getTurnoutStatus(1)==THROWN) {
               newCommandedState(THROWN);
               newKnownState(getCommandedState());
            } else if(l.getTurnoutStatus(1)==CLOSED) { 
               newCommandedState(CLOSED);
               newKnownState(getCommandedState());
            } else return -1;
        } else if (((mNumber%2)==0) && 
                   (l.getTurnoutMsgAddr() == mNumber-1)) {
            // is for this object, parse message type
            if (log.isDebugEnabled()) log.debug("Message for turnout" + mNumber);
            if(l.getTurnoutStatus(0)==THROWN) {
               newCommandedState(THROWN);
               newKnownState(getCommandedState());
            } else if(l.getTurnoutStatus(0)==CLOSED) { 
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
     private boolean motionComplete(XNetReply l) {
        // check validity & addressing
        // if this is an ODD numbered turnout, then we always get the 
        // right response from .getTurnoutMsgAddr.  If this is an even 
        // numbered turnout, we need to check the messages for the odd 
        // numbered turnout in the nibble as well.
        if (mNumber%2==1 && (l.getTurnoutMsgAddr() == mNumber)) {
            // is for this object, parse the message
            int messageType= l.getFeedbackMessageType();
	  if(messageType == 1) {
             int a2=l.getElement(2);
             if((a2 & 0x80)==0x80) { return false;
             } else { return true; }
	  } else return false;
        } else if (((mNumber%2)==0) && 
                   (l.getTurnoutMsgAddr() == mNumber-1)) {
            // is for this object, parse the message
          int messageType= l.getFeedbackMessageType();
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
	this.removePropertyChangeListener(_stateListener);
    }

   
    // Internal class to use for listening to state changes
    private class XNetTurnoutStateListener implements java.beans.PropertyChangeListener {

    XNetTurnout _turnout=null;

    XNetTurnoutStateListener(XNetTurnout turnout){
	_turnout=turnout;
    }

    /*
     * If we're  not using DIRECT feedback mode, we need to listen for 
     * state changes to know when to send an OFF message after we set the 
     * known state
     * If we're using DIRECT mode, all of this is handled from the 
     * XPressNet Messages
     */
    public void propertyChange(java.beans.PropertyChangeEvent event) {
	if(log.isDebugEnabled()) log.debug("propertyChange called");
	// If we're using DIRECT feedback mode, we don't care what we see here
	if(_turnout.getFeedbackMode()!=DIRECT) {
	   if(event.getPropertyName().equals("KnownState")) {
		// Check to see if this is a change in the status 
		// triggered by a device on the layout, or a change in 
		// status we triggered.
		int oldKnownState=((Integer)event.getOldValue()).intValue();
		int curKnownState=((Integer)event.getNewValue()).intValue();
		if(_turnout.getCommandedState()==oldKnownState) {
		   // This was triggered by feedback on the layout, change 
		   // the commanded state to reflect the new Known State
               	   _turnout.newCommandedState(curKnownState);
		} else {
		   // Since we always set the KnownState to 
		   // INCONSISTENT when we send a command, If the old 
		   // known state is INCONSISTENT, we just want to send 
                   // an off message
		   if(oldKnownState==INCONSISTENT){
		   	_turnout.sendOffMessage();
		   }
		}
	    }
	}	
    }
    
    }

    // data members
    int mNumber;   // XPressNet turnout number
    XNetTurnoutStateListener _stateListener;  // Internal class object

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTurnout.class.getName());

}


/* @(#)XNetTurnout.java */

