// LnTurnout.java

package jmri.jmrix.loconet;

import jmri.AbstractTurnout;

/**
 * Extend jmri.AbstractTurnout for LocoNet layouts
 * <P>
 * This implementation implements the "SENT" feedback,
 * where LocoNet messages originating on the layout can change both KnownState
 * and CommandedState.  We change both because we consider a LocoNet message to
 * reflect how the turnout should be, even if its a readback status message.
 * E.g. if you use a DS54 local input to change the state, resulting in a
 * status message, we still consider that to be a commanded state change.
 * <P>
 * Adds several additional feedback modes:
 *<UL>
 *<LI>MONITORING - listen to the LocoNet, so that commands
 * from other LocoNet sources (e.g. throttles) are properly reflected
 * in the turnout state.  This is the default for LnTurnout objects
 * as created.
 *<LI>INDIRECT - listen to the LocoNet for messages back from a
 *DS54 that has a microswitch attached to its Switch input.
 *<LI>EXACT - listen to the LocoNet for messages back from a 
 * DS54 that has two microswitches, one connected to the Switch input
 * and one to the Aux input.  Note that this implementation does not
 * pass through the "UNKNOWN" or "INCONSISTENT" states while moving from 
 * "THROWN" to "CLOSED" or vice versa. To do that, one would have to
 * add input state tracking information.
 *</UL>
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.10 $
 */public class LnTurnout extends AbstractTurnout implements LocoNetListener {

    public LnTurnout(int number) {  // a human-readable turnout number must be specified!
        super("LT"+number);  // can't use prefix here, as still in construction
        log.debug("new turnout "+number);
         _number = number;
         // At construction, register for messages
         if (LnTrafficController.instance()!=null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.warn("No LocoNet connection, turnout won't update");
        // update feedback modes
        _validFeedbackTypes |= MONITORING|EXACT|INDIRECT;
        _activeFeedbackType = MONITORING;
        
        // define the static list if needed
        if (modeNames == null) {
        
            if (_validFeedbackNames.length != _validFeedbackModes.length)
                log.error("int and string feedback arrays different length");
            modeNames  = new String[_validFeedbackNames.length+3];
            modeValues = new int[_validFeedbackNames.length+3];
            for (int i = 0; i<_validFeedbackNames.length; i++) {
                modeNames[i] = _validFeedbackNames[i];
                modeValues[i] = _validFeedbackModes[i];
            }
            modeNames[_validFeedbackNames.length] = "MONITORING";
            modeValues[_validFeedbackNames.length] = MONITORING;
            modeNames[_validFeedbackNames.length+1] = "INDIRECT";
            modeValues[_validFeedbackNames.length+1] = INDIRECT;
            modeNames[_validFeedbackNames.length+2] = "EXACT";
            modeValues[_validFeedbackNames.length+2] = EXACT;
        }
        _validFeedbackNames = modeNames;
        _validFeedbackModes = modeValues;
     }

     static String[] modeNames = null;
     static int[] modeValues = null;
     
     public int getNumber() { return _number; }

     // Handle a request to change state by sending a LocoNet command
     protected void forwardCommandChangeToLayout(int s) {
         // send SWREQ for close
         LocoNetMessage l = new LocoNetMessage(4);
         l.setOpCode(LnConstants.OPC_SW_REQ);

         // compute address fields
         int hiadr = (_number-1)/128;
         int loadr = (_number-1)-hiadr*128;

         // set closed (note that this can't handle both!  Not sure how to
         // say that in LocoNet.
         if ((s & CLOSED) != 0) {
             hiadr |= 0x20;
             // thrown exception if also THROWN
             if ((s & THROWN) != 0)
                 log.error("LocoNet turnout logic can't handle both THROWN and CLOSED yet");
         }

         // load On/Off with on
         hiadr |= 0x10;

         // store and send
         l.setElement(1,loadr);
         l.setElement(2,hiadr);
         LnTrafficController.instance().sendLocoNetMessage(l);
     }

     // implementing classes will typically have a function/listener to get
     // updates from the layout, which will then call
     //		public void firePropertyChange(String propertyName,
     //					      	Object oldValue,
     //						Object newValue)
     // _once_ if anything has changed state (or set the commanded state directly)
     public void message(LocoNetMessage l) {
         // parse message type
         switch (l.getOpCode()) {
         case LnConstants.OPC_SW_REQ: {               /* page 9 of Loconet PE */
             int sw1 = l.getElement(1);
             int sw2 = l.getElement(2);
             if (myAddress(sw1, sw2)) {
                 if (log.isDebugEnabled()) log.debug("SW_REQ received with valid address");
                 if ((sw2 & LnConstants.OPC_SW_REQ_DIR)!=0) {
                     newCommandedState(CLOSED);
                     if (getFeedbackMode()==MONITORING || getFeedbackMode()==DIRECT) newKnownState(CLOSED);
                 } else {
                     newCommandedState(THROWN);
                     if (getFeedbackMode()==MONITORING || getFeedbackMode()==DIRECT) newKnownState(THROWN);
                 }
             }
             break;
         }
         case LnConstants.OPC_SW_REP: {               /* page 9 of Loconet PE */
             int sw1 = l.getElement(1);
             int sw2 = l.getElement(2);
             if (myAddress(sw1, sw2)) {
                 if (log.isDebugEnabled()) log.debug("SW_REP received with valid address");
                 // see if its a turnout state report
                 if ((sw2 & LnConstants.OPC_SW_REP_INPUTS)==0) {
                     // LnConstants.OPC_SW_REP_INPUTS not set, these report outputs
    	        		// sort out states
                     switch (sw2 &
                             (LnConstants.OPC_SW_REP_CLOSED|LnConstants.OPC_SW_REP_THROWN)) {

                     case LnConstants.OPC_SW_REP_CLOSED:
                         newCommandedState(CLOSED);
                         if (getFeedbackMode()==MONITORING || getFeedbackMode()==DIRECT) newKnownState(CLOSED);
                         break;
                     case LnConstants.OPC_SW_REP_THROWN:
                         newCommandedState(THROWN);
                         if (getFeedbackMode()==MONITORING || getFeedbackMode()==DIRECT) newKnownState(THROWN);
                         break;
                     case LnConstants.OPC_SW_REP_CLOSED|LnConstants.OPC_SW_REP_THROWN:
                         newCommandedState(CLOSED+THROWN);
                         if (getFeedbackMode()==MONITORING || getFeedbackMode()==DIRECT) newKnownState(CLOSED+THROWN);
                         break;
                     default:
                         newCommandedState(0);
                         if (getFeedbackMode()==MONITORING || getFeedbackMode()==DIRECT) newKnownState(0);
                         break;
                     }
                 } else {
                    // LnConstants.OPC_SW_REP_INPUTS set, these are feedback messages from inputs
    	        		// sort out states
                        // see EXACT feedback note at top    	  
    	        	if ((sw2 & LnConstants.OPC_SW_REP_SW) !=0) {
    	        	    // Switch input report
    	        	    if ((sw2 & LnConstants.OPC_SW_REP_HI)!=0) {
    	        	        // switch input closed (off)
    	        	        if (getFeedbackMode()==EXACT) {
    	        	            // reached closed state
    	        	            newKnownState(CLOSED);
    	        	        } else if (getFeedbackMode()==INDIRECT) {
    	        	            // reached closed state
    	        	            newKnownState(CLOSED);
    	        	        }
    	        	    } else {
    	        	        // switch input thrown (input on)
    	        	        if (getFeedbackMode()==EXACT) {
    	        	            // leaving CLOSED on way to THROWN, but ignoring that for now
    	        	        } else if (getFeedbackMode()==INDIRECT) {
    	        	            // reached thrown state
    	        	            newKnownState(THROWN);
    	        	        }
    	        	    }
    	        	} else {
    	        	    // Aux input report
    	        	    if ((sw2 & LnConstants.OPC_SW_REP_HI)!=0) {
    	        	        // aux input closed (off)
    	        	        if (getFeedbackMode()==EXACT) {
    	        	            // reached thrown state
    	        	            newKnownState(THROWN);
    	        	        }
    	        	    } else {
    	        	        // aux input thrown (input on)
    	        	        if (getFeedbackMode()==EXACT) {
    	        	            // leaving THROWN on the way to CLOSED, but ignoring that for now
    	        	        }
    	        	    }
    	        	}
    	        	
                 }
             }
         }
         default:
             return;
         }
         // reach here only in error
     }

     public void dispose() {
         LnTrafficController.instance().removeLocoNetListener(~0, this);
         super.dispose();
     }

     // data members
     int _number;   // loconet turnout number

     private boolean myAddress(int a1, int a2) {
         // the "+ 1" in the following converts to throttle-visible numbering
         return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) == _number;
     }
     static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnTurnout.class.getName());

 }

/* @(#)LnTurnout.java */
