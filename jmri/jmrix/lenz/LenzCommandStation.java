/*
 * LenzCommandStation.java
 */

package jmri.jmrix.lenz;


/**
 * Defines standard operations for Dcc command stations.
 *
 * @author			Bob Jacobsen Copyright (C) 2001 Portions by Paul Bender Copyright (C) 2003
 * @version			$Revision: 1.19 $
 */
public class LenzCommandStation implements jmri.jmrix.DccCommandStation {

    /**
     *  We need to add a few data members for saving the version 
     *  information we get from the layout.
     **/

     private int cmdStationType = -1;
     private float cmdStationSoftwareVersion = -1;

    /**
     * return the CS Type
     **/
     public int getCommandStationType() { return cmdStationType; }

    /**
     * set the CS Type
     **/
     public void setCommandStationType(int t) { cmdStationType = t; }

    /**
     * Set the CS type based on an XPressNet Message
     **/
     public void setCommandStationType(XNetMessage l) {
       if(l.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE)
       {
              // This is the Command Station Software Version Response
              if(l.getElement(1)==XNetConstants.CS_SOFTWARE_VERSION)
              {
                cmdStationType=l.getElement(3);
              }
       }
    }

    /**
     * return the CS Software Version
     **/
     public float getCommandStationSoftwareVersion() { return cmdStationSoftwareVersion; }

    /**
     * set the CS Software Version
     **/
     public void setCommandStationSoftwareVersion(float v) { cmdStationSoftwareVersion = v; }

    /**
     * Set the CS Software Version based on an XPressNet Message
     **/
     public void setCommandStationSoftwareVersion(XNetMessage l) {
       if(l.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE)
       {
              // This is the Command Station Software Version Response
              if(l.getElement(1)==XNetConstants.CS_SOFTWARE_VERSION)
              {
                cmdStationSoftwareVersion=(l.getElementBCD(2).floatValue())/10;
              }
       }
    }

    /**
     * Generate the message to request the Command Station 
     * Hardware/Software Version
     **/
     public XNetMessage getCSVersionRequestMessage() {
	XNetMessage msg=new XNetMessage(3);   
        msg.setElement(0,XNetConstants.CS_REQUEST);
        msg.setElement(1,XNetConstants.CS_VERSION);
        msg.setParity(); // Set the parity bit
        return msg;
     } 

    /**
     * Lenz does use a service mode
     */
    public boolean getHasServiceMode() {return true;}


    /**
     * If this command station has a service mode, is the command
     * station currently in that mode?
     */
    public boolean getInServiceMode() { return mInServiceMode; }
    
    /**
     * Provides the version string returned during the initial check.
     * This function is not yet implemented...
     **/
    public String getVersionString() { return "<unknown>"; }
    
    /**
     * Remember whether or not in service mode
     **/
    boolean mInServiceMode = false;
    
    /* The next group of routines are used by Feedback and/or turnout 
     * control code.  These are used in multiple places within the code, 
     * so they appear here. 
     */


    /**
     * Generate a message to change turnout state
     */
    public XNetMessage getTurnoutCommandMsg(int pNumber, boolean pClose,
                                            boolean pThrow, boolean pOn) {
        XNetMessage l = new XNetMessage(4);
        l.setElement(0, XNetConstants.ACC_OPER_REQ);
        
        // compute address byte fields
        int hiadr = (pNumber-1)/4;
        int loadr = ((pNumber-1)-hiadr*4)*2;
        // The MSB of the upper nibble is required to be set on
        // The rest of the upper nibble should be zeros.
        // The MSB of the lower nibble says weather or not the
        // accessory line should be "on" or off
        if (!pOn) { loadr |= 0x80; }
	else loadr |= 0x88;
        // If we are sending a "throw" command, we set the LSB of the 
        // lower nibble on, otherwise, we leave it off.
        if (pThrow) loadr |= 0x01;
        
        // we don't know how to command both states right now!
        if (pClose & pThrow)
            log.error("XPressNet turnout logic can't handle both THROWN and CLOSED yet");
        // store and send
        l.setElement(1,hiadr);
        l.setElement(2,loadr);
        
        return l;
    }

    /**
     * Generate a message to recieve the feedback information for an upper 
     * or lower nibble of the feedback address in question
     */
    public XNetMessage getFeedbackRequestMsg(int pNumber, 
                                             boolean pLowerNibble) {
        XNetMessage l = new XNetMessage(4);
        l.setElement(0, XNetConstants.ACC_INFO_REQ);
        
        // compute address byte field
        l.setElement(1,(pNumber-1)/4);
        // The MSB of the upper nibble is required to be set on
        // The rest of the upper nibble should be zeros.
        // The LSB of the lower nibble says weather or not the
        // information request is for the upper or lower nibble.
        if (pLowerNibble) { l.setElement(1,0x80);
	} else { l.setElement(2,0x81); }
        
        return l;
    }
    
    /**
     * If this is a feedback response message for a turnout, 
     * return the address.  Otherwise return -1.
     * Note we only identify the command here; the reponse to a
     * request for status is not interpreted here.
     */
    public int getTurnoutMsgAddr(XNetMessage pMsg) {
        if (isFeedbackMessage(pMsg)) {
            int a1 = pMsg.getElement(1);
            int a2 = pMsg.getElement(2);
            int messagetype=getFeedbackMessageType(pMsg);
	    if ( messagetype == 0 || messagetype == 1)
            {
               // This is a turnout message
               int address=(a1 & 0xff) * 4;
               if(((a2 & 0x13)==0x01) || ((a2 &0x13)==0x02)) {
                  // This is the first address in the group*/
                  return(address + 1);
               } else if(((a2 & 0x1c)==0x04) || ((a2 &0x1c)==0x08)) {
                  // This is the second address in the group
                  return(address + 2);
               } else if(((a2 & 0x13)==0x11) || ((a2 &0x13)==0x12)) {
                  // This is the third address in the group
                  return(address + 3);
               } else if(((a2 & 0x1c)==0x14) || ((a2 &0x1c)==0x18)) { 
                  // This is the fourth address in the group
                  return(address + 4);
     	       } else return -1;
            } else return -1;
        } else return -1;
    }

    /**
     * Parse the feedback message for a turnout, and return the status 
     * for the even or odd half of the nibble (upper or lower part)
     *
     * the turnout is identified by sending a 0 for even turnouts (the 
     * upper part of the nibble) or 1 for odd turnouts (the lower part of 
     * the nibble
     **/
     public int getTurnoutStatus(XNetMessage pMsg,int turnout) {
        if (isFeedbackMessage(pMsg)) {
            int a1 = pMsg.getElement(1);
            int a2 = pMsg.getElement(2);
            int messagetype=getFeedbackMessageType(pMsg);
	    if ( messagetype == 0 || messagetype == 1) {
 	       if (turnout==1) {
                  // we want the lower half of the nibble
                  if((a2 & 0x03)!=0) {
                     /* this is for the First turnout in the nibble */
                     int state=pMsg.getElement(2) & 0x03;
                     if(state==0x01) { 
                         return(jmri.Turnout.CLOSED);
                     } else if(state==0x02) { 
                         return(jmri.Turnout.THROWN);
                     } else return -1; /* the state is invalid */
                  }
               } else if (turnout==0) {
                  /* we want the upper half of the nibble */
                  if((a2 & 0x0C)!=0) {
                     /* this is for the upper half of the nibble */
                     int state=pMsg.getElement(2) & 0x0C;
                     if(state==0x04) { 
                         return(jmri.Turnout.CLOSED);  
                     } else if(state==0x08) { 
                         return (jmri.Turnout.THROWN);
                     } else return -1; /* the state is invalid */
                  }
               }
            }
         } 
         return(-1);
     }


    /**
     * If this is a feedback response message for a feedback encoder, 
     * return the address.  Otherwise return -1.
     * Note we only identify the command here; the reponse to a
     * request for status is not interpreted here.
     */
    public int getFeedbackEncoderMsgAddr(XNetMessage pMsg) {
        if (isFeedbackMessage(pMsg)) {
            int a1 = pMsg.getElement(1);
            int messagetype=getFeedbackMessageType(pMsg);
	    if ( messagetype == 2 )
            {
               // This is a feedback encoder message
               int address=(a1 & 0xff);
               return(address);
            } else return -1;
        } else return -1;
    }

    /**
     * Is this a feedback response message?
     */
    public boolean isFeedbackMessage(XNetMessage pMsg) {
        return (pMsg.getElement(0)==XNetConstants.ACC_INFO_RESPONSE);
    }


    /**
     * Extract the feedback message type from a feedback message
     * this is the middle two bits of the upper byte of the second data 
     * byte.  returned values are 0-3.
     * 0 for a turnout with no feedback
     * 1 for a turnout with feedback
     * 2 for a feedback encoder
     * 3 is reserved by Lenz for future use.
     */ 
    public int getFeedbackMessageType(XNetMessage pMsg) {
        if (isFeedbackMessage(pMsg)) {
            int a2 = pMsg.getElement(2);
            return((a2 & 0xff) / 16);            
	}
        else return -1;
    }

    /**
     * If this is a throttle-type message, return address. Otherwise
     * return -1.
     * Note we only identify the command now; the reponse to a
     * request for status is not yet seen here.
     */
    public int getThrottleMsgAddr(XNetMessage pMsg) {
        if (isThrottleCommand(pMsg)) {
            int a1 = pMsg.getElement(3);
            int a2 = pMsg.getElement(4);
            return ((a1 * 100) + a2);
        }
        else return -1;
    }
    
    /**
     * Is this a throttle message?
     */
    public boolean isThrottleCommand(XNetMessage pMsg) {
        int message=pMsg.getElement(0);
        if( message==0x83 || message==0x84 || message==0xA3 || 
            message == 0xA4 ||message == 0xE2 || message == 0xE3 || 
           message == 0xE4) return true;
        return false;
    }
    
    // start of programming messages
    public XNetMessage getServiceModeResultsMsg() {
        XNetMessage m = new XNetMessage(3);
        m.setElement(0, XNetConstants.CS_REQUEST);
        m.setElement(1, XNetConstants.SERVICE_MODE_CSRESULT);
        return m;
    }
    
    public XNetMessage getExitProgModeMsg() {
        XNetMessage m = new XNetMessage(3);
        m.setElement(0, XNetConstants.CS_REQUEST);
        m.setElement(1, XNetConstants.RESUME_OPS);
        return m;
    }
    
    public XNetMessage getReadPagedCVMsg(int cv) {
        XNetMessage m = new XNetMessage(4);
        m.setElement(0, XNetConstants.PROG_READ_REQUEST);
        m.setElement(1, XNetConstants.PROG_READ_MODE_PAGED);
        m.setElement(2, cv);
        return m;
    }
    
    public XNetMessage getReadDirectCVMsg(int cv) {
        XNetMessage m = new XNetMessage(4);
        m.setElement(0, XNetConstants.PROG_READ_REQUEST);
        m.setElement(1, XNetConstants.PROG_READ_MODE_CV);
        m.setElement(2, cv);
        return m;
    }
    
    public XNetMessage getWritePagedCVMsg(int cv, int val) {
        XNetMessage m = new XNetMessage(5);
        m.setElement(0, XNetConstants.PROG_WRITE_REQUEST);
        m.setElement(1, XNetConstants.PROG_WRITE_MODE_PAGED);
        m.setElement(2, cv);
        m.setElement(3, val);
        return m;
    }
    
    public XNetMessage getWriteDirectCVMsg(int cv, int val) {
        XNetMessage m = new XNetMessage(5);
        m.setElement(0, XNetConstants.PROG_WRITE_REQUEST);
        m.setElement(1, XNetConstants.PROG_WRITE_MODE_CV);
        m.setElement(2, cv);
        m.setElement(3, val);
        return m;
    }
    
    public XNetMessage getReadRegisterMsg(int reg) {
        if (reg>8) log.error("register number too large: "+reg);
        XNetMessage m = new XNetMessage(4);
        m.setElement(0, XNetConstants.PROG_READ_REQUEST);
        m.setElement(1, XNetConstants.PROG_READ_MODE_REGISTER);
        m.setElement(2, reg);
        return m;
    }
    
    public XNetMessage getWriteRegisterMsg(int reg, int val) {
        if (reg>8) log.error("register number too large: "+reg);
        XNetMessage m = new XNetMessage(5);
        m.setElement(0, XNetConstants.PROG_WRITE_REQUEST);
        m.setElement(1, XNetConstants.PROG_WRITE_MODE_REGISTER);
        m.setElement(2, reg);
        m.setElement(3, val);
        return m;
    }

    public XNetMessage getWriteOpsModeCVMsg(int AH,int AL,int cv, int val) {
        XNetMessage m = new XNetMessage(8);
        m.setElement(0, XNetConstants.OPS_MODE_PROG_REQ);
        m.setElement(1, XNetConstants.OPS_MODE_PROG_WRITE_REQ);
        m.setElement(2, AH);
        m.setElement(3, AL);
        /* Element 4 is 0xEC + the upper two  bits of the 10 bit CV address.
        NOTE: This is the track packet CV, not the human readable CV, so 
        it's value actually is one less than what we normally think of it as.*/
        int temp=(cv -1) & 0x0300;
        temp=temp/0x00FF;
        m.setElement(4,0xEC+temp);
        /* Element 5 is the lower 8 bits of the cv */
        m.setElement(5, (cv-1)-temp);
        m.setElement(6, val);
        return m;
    }

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is the OK message (01 04 05)
     */
    public boolean isOkMessage(XNetMessage m) {
        return (m.getElement(0)==XNetConstants.LI_MESSAGE_RESPONSE_HEADER && 
                m.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS);
    }	

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LenzCommandStation.class.getName());
    
}


/* @(#)LenzCommandStation.java */
