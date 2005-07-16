// XNetReply.java

package jmri.jmrix.lenz;

/**
 * Represents a single response from the XpressNet.
 *<P>
 *
 * @author			Paul Bender Copyright (C) 2004
 * @version			$Revision: 1.4 $
 *
 */
public class XNetReply extends jmri.jmrix.AbstractMRReply {

   // Create a new reply.
    public XNetReply() {
       super();
       setBinary(true);
    }

   // Create a new reply from an existing reply
    public XNetReply(XNetReply reply) {
       super(reply);
       setBinary(true);
    }

   /** 
    * Create a reply from an XNetMessage.
    */
    public XNetReply(XNetMessage message) {
       super(); 
       setBinary(true);
       for(int i=0;i<message.getNumDataElements();i++)
       {
	  setElement(i,message.getElement(i));
       }
    }


    /* Get the opcode as a string in hex format */
    public String getOpCodeHex() { return "0x"+Integer.toHexString(this.getOpCode()); }

        /**
         * check whether the message has a valid parity
         */
        public boolean checkParity() {
                int len = getNumDataElements();
                int chksum = 0x00;  /* the seed */
                int loop;
        
        for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
                chksum ^= getElement(loop);
        }
                return ((chksum&0xFF) == getElement(len-1));
        }
    
    public void setParity() {
                int len = getNumDataElements();   
                int chksum = 0x00;  /* the seed */

                int loop;

        for(loop = 0; loop < len-1; loop++) {  // calculate contents for data part
                chksum ^= getElement(loop);
        }
                setElement(len-1, chksum&0xFF);
    }
 
    /** Get an integer representation of a BCD value */
        public Integer getElementBCD(int n) { return Integer.decode(Integer.toHexString(getElement(n))); }

    /* 
     * skipPrefix is not used at this point in time, but is 
     *  defined as abstract in AbstractMRReply 
     */
    protected int skipPrefix(int index) { return -1;}
   
    // decode messages of a particular form 

    
    /* 
     * The next group of routines are used by Feedback and/or turnout 
     * control code.  These are used in multiple places within the code, 
     * so they appear here. 
     */

    /**
     * If this is a feedback response message for a turnout, 
     * return the address.  Otherwise return -1.
     * Note we only identify the command here; the reponse to a
     * request for status is not interpreted here.
     */
    public int getTurnoutMsgAddr() {
        if (this.isFeedbackMessage()) {
            int a1 = this.getElement(1);
            int a2 = this.getElement(2);
            int messagetype=this.getFeedbackMessageType();
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
     public int getTurnoutStatus(int turnout) {
        if (this.isFeedbackMessage()) {
            int a1 = this.getElement(1);
            int a2 = this.getElement(2);
            int messagetype=this.getFeedbackMessageType();
	    if ( messagetype == 0 || messagetype == 1) {
 	       if (turnout==1) {
                  // we want the lower half of the nibble
                  if((a2 & 0x03)!=0) {
                     /* this is for the First turnout in the nibble */
                     int state=this.getElement(2) & 0x03;
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
                     int state=this.getElement(2) & 0x0C;
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
    public int getFeedbackEncoderMsgAddr() {
        if (this.isFeedbackMessage()) {
            int a1 = this.getElement(1);
            int messagetype=this.getFeedbackMessageType();
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
    public boolean isFeedbackMessage() {
        return (this.getElement(0)==XNetConstants.ACC_INFO_RESPONSE);
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
    public int getFeedbackMessageType() {
        if (this.isFeedbackMessage()) {
            int a2 = this.getElement(2);
            return((a2 & 0x60) / 32);            
	}
        else return -1;
    }

    /* 
     * Next we have a few throttle related messages
     */

    /**
     * If this is a throttle-type message, return address. Otherwise
     * return -1.
     * Note we only identify the command now; the reponse to a
     * request for status is not yet seen here.
     */
    public int getThrottleMsgAddr() {
        if (this.isThrottleCommand()) {
            int a1 = this.getElement(2);
            int a2 = this.getElement(3);
	    if(a1==0) 
	       return(a2);
	    else 	
               return (((a1 * 256) & 0xFF00) + (a2 &0xFF) - 0xC000);
        }
        else return -1;
    }
    
    /**
     * Is this a throttle message?
     */
    public boolean isThrottleCommand() {
        int message=this.getElement(0);
        if( message==0x83 || message==0x84 || message==0xA3 || 
            message == 0xA4 ||message == 0xE2 || message == 0xE3 || 
           message == 0xE4) return true;
        return false;
    }
    
    /* 
     * Finally, we have some commonly used routines that are used for 
     * checking specific, generic, response messages.
     */

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is the OK message (01 04 05)
     */
    public boolean isOkMessage() {
        return (this.getElement(0)==XNetConstants.LI_MESSAGE_RESPONSE_HEADER && 
                this.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS);
    }	

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is the Command Station Busy message (61 81 e3)
     */
    public boolean isCSBusyMessage() {
        return (this.getElement(0)==XNetConstants.CS_INFO && 
                this.getElement(1)==XNetConstants.CS_BUSY);
    }	

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is a communications error message.
     * the errors handeled are:
     *		01 01 00  -- Error between interface and the PC
     *		01 02 03  -- Error between interface and the Command Station
     *		01 03 02  -- Unknown Communications Error
     *		01 05 04  -- Timeslot Error
     *          01 06 07  -- LI10x Buffer Overflow
     */
    public boolean isCommErrorMessage() {
	return (this.getElement(0)==XNetConstants.LI_MESSAGE_RESPONSE_HEADER &&
               ((this.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR ||
                 this.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR ||
                 this.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR ||
                 this.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_BUFFER_OVERFLOW ||
                 this.getElement(1)==XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR)));
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetReply.class.getName());

}

/* @(#)XNetMessage.java */
